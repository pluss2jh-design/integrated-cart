import { useState } from 'react';

/**
 * 장바구니 모달 컴포넌트.
 * 담긴 상품을 쇼핑몰별로 그룹화하여 표시하고 원클릭 결제를 제공합니다.
 * @param {Object} props
 * @param {Function} props.onClose - 모달 닫기 콜백
 * @param {Array} props.cartItems - 장바구니 상품 배열
 * @param {string} props.apiBase - API 기본 URL
 * @param {Function} props.onRemoveItem - 아이템 삭제 콜백
 * @returns {JSX.Element} 장바구니 모달 UI
 */
export default function CartModal({ onClose, cartItems, apiBase, onRemoveItem }) {
    const [isProcessing, setIsProcessing] = useState(false);

    // 쇼핑몰 타입별로 상품 그룹화
    const groupedItems = cartItems.reduce((acc, { product, quantity }) => {
        if (!acc[product.mallType]) acc[product.mallType] = [];
        acc[product.mallType].push({ product, quantity });
        return acc;
    }, {});

    const totalAmount = cartItems.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);

    const handleAutoCart = async () => {
        setIsProcessing(true);
        try {
            const mallTypes = Object.keys(groupedItems);
            const res = await fetch(`${apiBase}/order/auto-cart`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId: 1, mallTypes: mallTypes })
            });
            if (!res.ok) throw new Error("자동 담기 실패");

            alert("선택한 쇼핑몰의 장바구니에 상품을 모두 담았습니다! 곧 장바구니 페이지가 열립니다.");
            onClose();
        } catch (e) {
            alert("장바구니 연동 중 오류 발생: " + e.message);
        } finally {
            setIsProcessing(false);
        }
    };

    const handleCheckout = async () => {
        setIsProcessing(true);
        try {
            const mallTypesForCheckout = Object.keys(groupedItems);
            const res = await fetch(`${apiBase}/order/checkout`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId: 1, mallTypes: mallTypesForCheckout })
            });
            if (!res.ok) throw new Error("결제 시작 실패");

            alert("각 쇼핑몰 봇이 백그라운드에서 결제를 시작했습니다!");
            onClose();
        } catch (e) {
            alert("결제 연동 중 오류 발생");
        } finally {
            setIsProcessing(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
            <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-200">

                <div className="p-6 border-b border-gray-100 flex justify-between items-center bg-gray-50/50">
                    <h2 className="text-xl font-bold text-gray-800">통합 장바구니</h2>
                    <button onClick={onClose} className="p-2 text-gray-400 hover:text-gray-600 rounded-full hover:bg-gray-100 transition-colors">
                        ✕
                    </button>
                </div>

                <div className="flex-1 overflow-y-auto p-6 space-y-6">
                    {cartItems.length === 0 ? (
                        <p className="text-gray-500 text-center py-8">장바구니가 비어있습니다.</p>
                    ) : (
                        Object.keys(groupedItems).map(mall => (
                            <div key={mall} className="space-y-4">
                                <h3 className="font-semibold text-gray-700 pb-2 border-b border-gray-100">
                                    {mall === 'COUPANG' ? '쿠팡' : mall === 'KURLY' ? '마켓컬리' : 'B마트'} 로켓배송
                                </h3>
                                {groupedItems[mall].map((item, idx) => (
                                    <div key={idx} className="flex gap-4 items-center">
                                        <div className="w-16 h-16 bg-gray-100 rounded-lg flex-shrink-0"></div>
                                        <div className="flex-1">
                                            <p className="text-sm font-medium text-gray-800 line-clamp-1">{item.product.name}</p>
                                            <p className="text-sm text-gray-500">{item.product.capacity}{item.product.unit} × {item.quantity}개</p>
                                        </div>
                                        <div className="text-right flex flex-col items-end gap-2">
                                            <p className="font-bold text-gray-900">{(item.product.price * item.quantity).toLocaleString()}원</p>
                                            <button 
                                                onClick={() => onRemoveItem(cartItems.indexOf(item))}
                                                className="text-[10px] text-red-400 hover:text-red-600 font-bold uppercase"
                                            >
                                                삭제
                                            </button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ))
                    )}
                </div>

                {cartItems.length > 0 && (
                    <div className="p-6 border-t border-gray-100 bg-white">
                        <div className="flex justify-between items-center mb-6">
                            <span className="text-gray-600 font-medium">총 결제예상금액 </span>
                            <span className="text-2xl font-bold text-indigo-600">{totalAmount.toLocaleString()}원</span>
                        </div>
                        <div className="flex gap-4">
                            <button
                                onClick={handleAutoCart}
                                disabled={isProcessing}
                                className="flex-1 py-4 bg-slate-800 hover:bg-slate-900 text-white rounded-xl font-bold text-lg shadow-sm transition-all disabled:opacity-75"
                            >
                                {isProcessing ? '처리 중...' : '전체 쇼핑몰 장바구니 담기'}
                            </button>
                            <button
                                onClick={handleCheckout}
                                disabled={isProcessing}
                                className="flex-1 py-4 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold text-lg shadow-sm transition-all disabled:opacity-75"
                            >
                                {isProcessing ? '처리 중...' : '원클릭 결제'}
                            </button>
                        </div>
                    </div>
                )}

            </div>
        </div>
    );
}
