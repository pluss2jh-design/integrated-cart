import { useState } from 'react';
import SearchBar from './components/SearchBar';
import ResultList from './components/ResultList';
import CartModal from './components/CartModal';

function App() {
    const [analyzing, setAnalyzing] = useState(false);
    const [recipe, setRecipe] = useState(null);
    const [products, setProducts] = useState(null);
    const [isCartOpen, setIsCartOpen] = useState(false);
    const [cartItems, setCartItems] = useState([]);

    // 임시 연결용 API URL - 실제 환경에서는 환경 변수 사용 권장
    const API_BASE = 'http://localhost:8080/api/v1';

    const handleAnalyze = async (url) => {
        setAnalyzing(true);
        try {
            // 1. 레시피 분석 (Mocking)
            const res = await fetch(`${API_BASE}/analyze`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ input: url })
            });
            if (!res.ok) throw new Error("분석 실패");

            const recipeData = await res.json();
            setRecipe(recipeData);

            // 2. 추출된 재료 중 첫 번째 재료로 자동 검색 (데모용)
            if (recipeData.ingredientsJson) {
                const ingredients = JSON.parse(recipeData.ingredientsJson);
                if (ingredients.length > 0) {
                    handleSearch(ingredients[0].name, ingredients[0].amount, false);
                }
            } else {
                handleSearch("양파", 100, false);
            }

        } catch (error) {
            console.error(error);
            alert("분석 중 오류가 발생했습니다.");
        } finally {
            setAnalyzing(false);
        }
    };

    const [searchError, setSearchError] = useState(false);

    const handleSearch = async (keyword, amount, isLowSugar) => {
        setSearchError(false);
        try {
            const res = await fetch(`${API_BASE}/ingredients/search?keyword=${keyword}&requiredAmount=${amount}&lowSugar=${isLowSugar}`);
            if (!res.ok) throw new Error("검색 실패");
            const searchResults = await res.json();

            // 모든 몰의 결과가 비어있는지 확인
            const isEmpty = Object.values(searchResults).every(arr => arr.length === 0);
            if (isEmpty) {
                setSearchError(true);
            }
            setProducts(searchResults);
        } catch (e) {
            console.error("검색 오류", e);
            setSearchError(true);
        }
    };

    const addToCart = async (product, quantity) => {
        try {
            // API Call로 장바구니 추가
            await fetch(`${API_BASE}/cart/add`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId: 1, productId: product.id, quantity })
            });
            // 로컬 장바구니 상태 업데이트
            setCartItems(prev => [...prev, { product, quantity }]);
            alert("장바구니에 담겼습니다.");
        } catch (e) {
            console.error("카트 추가 실패", e);
        }
    };

    return (
        <div className="min-h-screen relative p-4 md:p-8">
            <header className="flex justify-between items-center mb-8 max-w-5xl mx-auto">
                <h1 className="text-3xl font-bold text-gray-800">Integrated Cart</h1>
                <button
                    onClick={() => setIsCartOpen(true)}
                    className="bg-primary-500 hover:bg-primary-600 text-white px-4 py-2 rounded-lg shadow transition-colors flex items-center gap-2">
                    <span>🛒 장바구니</span>
                    {cartItems.length > 0 && (
                        <span className="bg-red-500 text-white text-xs px-2 py-1 rounded-full">{cartItems.length}</span>
                    )}
                </button>
            </header>

            <main className="max-w-5xl mx-auto space-y-8">
                <SearchBar onAnalyze={handleAnalyze} isLoading={analyzing} />

                {recipe && (
                    <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
                        <h2 className="text-xl font-semibold mb-2">분석된 레시피: {recipe.name}</h2>
                        <p className="text-gray-600">기준 인분: {recipe.basePortion}인분</p>
                    </div>
                )}

                {searchError && (
                    <div className="bg-red-50 border border-red-200 text-red-700 p-6 rounded-xl text-center">
                        <p className="font-bold text-lg">⚠️ 재료 검색에 실패했습니다.</p>
                        <p className="text-sm mt-1">마트 검색 결과가 없거나 일시적인 오류입니다. 다른 키워드로 시도해보세요.</p>
                    </div>
                )}

                {products && !searchError && (
                    <ResultList productsByMall={products} onAddToCart={addToCart} />
                )}
            </main>

            {isCartOpen && (
                <CartModal
                    onClose={() => setIsCartOpen(false)}
                    cartItems={cartItems}
                />
            )}
        </div>
    );
}

export default App;
