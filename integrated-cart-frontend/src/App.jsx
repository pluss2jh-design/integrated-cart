import { useState, useEffect } from 'react';
import SearchBar from './components/SearchBar';
import ResultList from './components/ResultList';
import CartModal from './components/CartModal';
import { getSearchLink } from './utils/mall-links';

/**
 * 통합 장바구니 앱의 메인 컴포넌트.
 * 레시피 분석, 재료 검색, 장바구니 관리 기능을 제공합니다.
 * @returns {JSX.Element} 앱 루트 컴포넌트
 */
function App() {
    // 로딩 및 데이터 상태
    const [analyzing, setAnalyzing] = useState(false);
    const [productLoading, setProductLoading] = useState(false);
    const [searchError, setSearchError] = useState(false);
    
    // 비즈니스 데이터 상태
    const [recipe, setRecipe] = useState(null);
    const [ingredients, setIngredients] = useState([]);
    const [selectedIngredient, setSelectedIngredient] = useState(null);
    const [products, setProducts] = useState(null);
    
    // UI 및 설정 상태
    const [isCartOpen, setIsCartOpen] = useState(false);
    const [cartItems, setCartItems] = useState([]);
    const [portion, setPortion] = useState(1);
    const [selectedMalls, setSelectedMalls] = useState(['ALL']);

    // API 기본 URL (환경변수 VITE_API_URL로 설정 가능)
    const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

    /**
     * 음식명 또는 URL을 입력받아 AI로 레시피를 분석합니다.
     */
    const handleAnalyze = async (input, p, malls, modelName) => {
        setAnalyzing(true);
        setSearchError(false);
        setProducts(null);
        setRecipe(null);
        setIngredients([]);
        setPortion(p);
        setSelectedMalls(malls);

        try {
            const res = await fetch(`${API_BASE}/analyze`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ input: input, modelName: modelName })
            });
            
            if (!res.ok) {
                const errorData = await res.json().catch(() => ({}));
                throw new Error(errorData.message || "레시피 분석에 실패했습니다.");
            }

            const recipeData = await res.json();
            setRecipe(recipeData);

            if (recipeData.ingredientsJson) {
                let baseIngredients = [];
                try {
                    baseIngredients = typeof recipeData.ingredientsJson === 'string'
                        ? JSON.parse(recipeData.ingredientsJson)
                        : recipeData.ingredientsJson;
                } catch (e) {
                    console.error("재료 JSON 파싱 오류:", e);
                    baseIngredients = [];
                }

                // 인분 비율에 따른 수량 조절
                const ratio = p / recipeData.basePortion;
                const scaledIngredients = baseIngredients.map(ing => ({
                    ...ing,
                    amount: Math.round(ing.amount * ratio * 10) / 10
                }));

                setIngredients(scaledIngredients);

                // 첫 번째 재료 자동 검색
                if (scaledIngredients.length > 0) {
                    setSelectedIngredient(scaledIngredients[0]);
                    await handleSearch(scaledIngredients[0].name, scaledIngredients[0].amount, malls);
                }
            }
        } catch (error) {
            alert(error.message);
            setSearchError(true);
        } finally {
            setAnalyzing(false);
        }
    };

    /**
     * 특정 재료에 대한 상품 검색을 수행합니다.
     */
    const handleSearch = async (keyword, amount, malls) => {
        setSearchError(false);
        setProducts(null);
        setProductLoading(true);
        try {
            const mallParams = malls.includes('ALL') ? '' : `&malls=${malls.join(',')}`;
            const res = await fetch(`${API_BASE}/ingredients/search?keyword=${encodeURIComponent(keyword)}&requiredAmount=${amount}${mallParams}`);
            
            if (!res.ok) throw new Error("상품 검색에 실패했습니다.");

            const searchResults = await res.json();

            const isEmpty = Object.values(searchResults).every(arr => arr.length === 0);
            if (isEmpty) {
                setSearchError(true);
            }
            setProducts(searchResults);
        } catch (e) {
            setSearchError(true);
        } finally {
            setProductLoading(false);
        }
    };

    const selectIngredient = (ing) => {
        setSelectedIngredient(ing);
        handleSearch(ing.name, ing.amount, selectedMalls);
    };

    const addToCart = (product, quantity) => {
        setCartItems(prev => [...prev, { product, quantity }]);
    };

    const removeFromCart = (index) => {
        setCartItems(prev => prev.filter((_, i) => i !== index));
    };

    return (
        <div className="min-h-screen bg-[#f8fafc] p-4 md:p-8 font-sans text-slate-900">
            <header className="flex justify-between items-center mb-10 max-w-6xl mx-auto py-4">
                <div className="flex items-center gap-3">
                    <div className="w-12 h-12 bg-indigo-600 rounded-2xl flex items-center justify-center shadow-xl shadow-indigo-200 transform rotate-3">
                        <span className="text-white font-black text-2xl">I</span>
                    </div>
                    <div>
                        <h1 className="text-2xl font-black tracking-tight text-slate-800">INTEGRATED CART</h1>
                        <p className="text-[10px] font-bold text-indigo-500 tracking-[0.2em] uppercase">Smart Shopping Assistant</p>
                    </div>
                </div>
                <button
                    onClick={() => setIsCartOpen(true)}
                    className="bg-white hover:border-indigo-200 text-slate-700 px-6 py-3 rounded-2xl shadow-sm border border-slate-200 transition-all flex items-center gap-3 font-bold group">
                    <span className="text-xl group-hover:scale-110 transition-transform">🛒</span>
                    <span>장바구니</span>
                    {cartItems.length > 0 && (
                        <span className="bg-indigo-600 text-white text-xs px-2 py-1 rounded-full shadow-md animate-bounce">{cartItems.length}</span>
                    )}
                </button>
            </header>

            <main className="max-w-6xl mx-auto space-y-8">
                <SearchBar onAnalyze={handleAnalyze} isLoading={analyzing} />

                {recipe && (
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                        {/* 레시피 및 재료 리스트 */}
                        <div className="lg:col-span-1 space-y-6">
                            <div className="bg-white p-6 rounded-3xl shadow-sm border border-slate-100 overflow-hidden relative">
                                <div className="absolute top-0 left-0 w-full h-1 bg-indigo-500"></div>
                                <div className="mb-6">
                                    <span className="text-[10px] font-black text-indigo-500 bg-indigo-50 px-2 py-1 rounded uppercase tracking-wider mb-2 inline-block">분석된 레시피</span>
                                    <h2 className="text-2xl font-black text-slate-800">{recipe.name}</h2>
                                    <p className="text-slate-400 text-sm font-medium mt-1">기준: {portion}인분</p>
                                </div>
                                <div className="space-y-2">
                                    <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-3">필요 재료 리스트</h3>
                                    {ingredients.map((ing, idx) => (
                                        <div
                                            key={idx}
                                            onClick={() => selectIngredient(ing)}
                                            className={`group p-4 rounded-2xl cursor-pointer transition-all border-2 ${selectedIngredient?.name === ing.name
                                                    ? 'border-indigo-500 bg-indigo-50 shadow-sm'
                                                    : 'border-transparent bg-slate-50 hover:bg-slate-100'
                                                }`}
                                        >
                                            <div className="flex justify-between items-center mb-2">
                                                <span className="font-bold text-slate-700">{ing.name}</span>
                                                <span className="text-sm font-black text-indigo-600 bg-white px-2 py-1 rounded-lg border border-slate-100">
                                                    {ing.amount}{ing.unit}
                                                </span>
                                            </div>
                                            <div className="flex gap-1.5 opacity-0 group-hover:opacity-100 transition-opacity">
                                                {['KURLY', 'NAVER', 'COUPANG', 'BMART'].map(m => (
                                                    <a
                                                        key={m}
                                                        href={getSearchLink(m, ing.name)}
                                                        target="_blank"
                                                        rel="noopener noreferrer"
                                                        onClick={(e) => e.stopPropagation()}
                                                        className="w-6 h-6 rounded-lg flex items-center justify-center text-[10px] font-black border border-slate-200 bg-white text-slate-400 hover:text-indigo-600 hover:border-indigo-200 transition-colors"
                                                    >
                                                        {m === 'BMART' ? 'B' : m[0]}
                                                    </a>
                                                ))}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* 검색 결과 */}
                        <div className="lg:col-span-2 space-y-6">
                            {selectedIngredient && (
                                <div className="flex items-center justify-between px-2">
                                    <h3 className="font-black text-slate-800 text-xl flex items-center gap-3">
                                        <span className="w-1.5 h-6 bg-indigo-500 rounded-full"></span>
                                        <span className="text-indigo-600">'{selectedIngredient.name}'</span> 최저가 결과
                                    </h3>
                                    <div className="flex items-center gap-2 text-[10px] font-bold text-slate-400 bg-white px-3 py-1.5 rounded-full border border-slate-100 shadow-sm">
                                        <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                                        REAL-TIME PRICES
                                    </div>
                                </div>
                            )}

                            {searchError ? (
                                <div className="bg-white border-2 border-dashed border-slate-200 p-16 rounded-3xl text-center space-y-4">
                                    <div className="text-6xl grayscale opacity-50 mb-4">🔍</div>
                                    <h3 className="font-black text-slate-800 text-xl">'{selectedIngredient?.name}' 검색 결과가 없습니다.</h3>
                                    <p className="text-slate-500 max-w-sm mx-auto text-sm leading-relaxed">
                                        선택하신 마트에 해당 재료의 개별 상품이 없거나 일시적으로 조회가 불가능합니다. 키워드를 변경하거나 다른 마트를 선택해 보세요.
                                    </p>
                                    <button
                                        onClick={() => handleSearch(selectedIngredient?.name.split(' ')[0], selectedIngredient?.amount, selectedMalls)}
                                        className="text-indigo-600 font-bold text-sm bg-indigo-50 px-4 py-2 rounded-xl hover:bg-indigo-100 transition-colors"
                                    >
                                        키워드 단순화해서 재검색 (예: {selectedIngredient?.name.split(' ')[0]})
                                    </button>
                                </div>
                            ) : products ? (
                                <ResultList productsByMall={products} onAddToCart={addToCart} keyword={selectedIngredient?.name} />
                            ) : (productLoading || analyzing) ? (
                                <div className="space-y-4">
                                    {[1, 2, 3].map(i => (
                                        <div key={i} className="h-48 bg-slate-200 rounded-3xl animate-pulse"></div>
                                    ))}
                                </div>
                            ) : (
                                <div className="bg-indigo-600 p-12 rounded-[2.5rem] text-center text-white space-y-6 shadow-2xl shadow-indigo-200 overflow-hidden relative">
                                    <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full blur-3xl -mr-32 -mt-32"></div>
                                    <div className="absolute bottom-0 left-0 w-64 h-64 bg-indigo-400/20 rounded-full blur-3xl -ml-32 -mb-32"></div>

                                    <div className="text-5xl mb-4">👨‍🍳</div>
                                    <h3 className="font-black text-3xl">무엇을 요리하시겠어요?</h3>
                                    <p className="text-indigo-100 font-medium text-lg leading-relaxed">
                                        음식명이나 레시피 링크를 입력하면 <br />
                                        <span className="text-white font-bold underline decoration-indigo-300 underline-offset-4">필수 재료만 쏙쏙 뽑아</span> 최저가를 찾아드려요.
                                    </p>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* 분석 전 초기 화면 */}
                {!recipe && !analyzing && (
                    <div className="max-w-4xl mx-auto mt-20 text-center space-y-12">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 text-left">
                            <div className="bg-white p-8 rounded-3xl shadow-sm border border-slate-100 hover:shadow-xl hover:-translate-y-1 transition-all">
                                <div className="text-3xl mb-4">🐷</div>
                                <h4 className="font-black text-slate-800 mb-2">제육볶음</h4>
                                <p className="text-sm text-slate-500 leading-relaxed italic">"뒷다리살 최저가부터 양파, 고추장까지 한 번에!"</p>
                            </div>
                            <div className="bg-white p-8 rounded-3xl shadow-sm border border-slate-100 hover:shadow-xl hover:-translate-y-1 transition-all">
                                <div className="text-3xl mb-4">🍲</div>
                                <h4 className="font-black text-slate-800 mb-2">김치찌개</h4>
                                <p className="text-sm text-slate-500 leading-relaxed italic">"잘 익은 김치와 찌개용 돼지고기를 마트별로 비교해보세요."</p>
                            </div>
                            <div className="bg-white p-8 rounded-3xl shadow-sm border border-slate-100 hover:shadow-xl hover:-translate-y-1 transition-all">
                                <div className="text-3xl mb-4">🎥</div>
                                <h4 className="font-black text-slate-800 mb-2">유튜브 레시피</h4>
                                <p className="text-sm text-slate-500 leading-relaxed italic">"링크만 붙여넣으세요. AI가 재료만 정확히 분석합니다."</p>
                            </div>
                        </div>
                    </div>
                )}
            </main>

            {isCartOpen && (
                <CartModal
                    onClose={() => setIsCartOpen(false)}
                    cartItems={cartItems}
                    apiBase={API_BASE}
                    onRemoveItem={removeFromCart}
                />
            )}
        </div>
    );
}

export default App;
