export default function ResultList({ productsByMall, onAddToCart }) {
    if (!productsByMall) return null;

    const malls = Object.keys(productsByMall);

    return (
        <div className="space-y-8">
            {malls.map(mallType => (
                <div key={mallType} className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
                    <h2 className="text-xl font-bold mb-6 text-gray-800 flex items-center gap-2">
                        <span className="p-2 bg-gray-100 rounded-lg">{mallType === 'COUPANG' ? '쿠팡' : mallType === 'KURLY' ? '마켓컬리' : 'B마트'}</span>
                        검색 결과
                    </h2>

                    {productsByMall[mallType].length === 0 ? (
                        <p className="text-gray-500 py-4 text-center">상품 검색 결과가 없습니다.</p>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {productsByMall[mallType].map(product => (
                                <div key={product.id} className="border border-gray-100 rounded-xl p-4 hover:shadow-md transition-shadow group">
                                    <div className="aspect-square bg-gray-50 rounded-lg mb-4 flex items-center justify-center text-gray-400 group-hover:scale-105 transition-transform">
                                        [상품 이미지]
                                    </div>
                                    <div className="space-y-1">
                                        <h3 className="font-semibold text-gray-800 line-clamp-2">{product.name}</h3>
                                        <p className="text-primary-600 font-bold text-lg">
                                            {product.price.toLocaleString()}원
                                        </p>
                                        <div className="text-sm text-gray-500 flex justify-between items-center mt-2">
                                            <span>{product.capacity}{product.unit}</span>
                                            {product.sugarPer100g !== null && (
                                                <span className="bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs font-medium">
                                                    당 {product.sugarPer100g}g/100g
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => onAddToCart(product, 1)}
                                        className="w-full mt-4 py-2.5 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors font-medium text-sm"
                                    >
                                        장바구니 담기
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            ))}
        </div>
    );
}
