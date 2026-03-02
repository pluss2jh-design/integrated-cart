import { getSearchLink } from '../utils/mall-links';

/**
 * 쇼핑몰별 상품 검색 결과를 표시하는 컴포넌트.
 * @param {Object} props
 * @param {Object} props.productsByMall - 쇼핑몰 타입별 상품 리스트
 * @param {Function} props.onAddToCart - 장바구니 담기 콜백
 * @param {string} props.keyword - 검색 키워드
 * @returns {JSX.Element|null} 검색 결과 UI
 */
export default function ResultList({ productsByMall, onAddToCart, keyword }) {
    if (!productsByMall) return null;

    const malls = Object.keys(productsByMall);

    return (
        <div className="space-y-8">
            {malls.map(mallType => (
                <div key={mallType} className="bg-white p-6 rounded-3xl shadow-sm border border-slate-100">
                    <div className="flex justify-between items-center mb-6">
                        <h2 className="text-xl font-bold text-gray-800 flex items-center gap-2">
                            <span className="p-2 bg-slate-50 rounded-xl border border-slate-100">
                                {mallType === 'COUPANG' ? '쿠팡' :
                                    mallType === 'KURLY' ? '마켓컬리' :
                                        mallType === 'NAVER' ? '네이버' : 'B마트'}
                            </span>
                            검색 결과
                        </h2>
                        <a
                            href={getSearchLink(mallType, keyword)}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-xs font-bold text-indigo-600 hover:underline flex items-center gap-1 bg-indigo-50 px-3 py-1.5 rounded-full"
                        >
                            {mallType === 'KURLY' ? '컬리' :
                                mallType === 'COUPANG' ? '쿠팡' :
                                    mallType === 'NAVER' ? '네이버' : 'B마트'}에서 직접 보기 ↗
                        </a>
                    </div>

                    {productsByMall[mallType].length === 0 ? (
                        <div className="py-12 text-center bg-slate-50 rounded-2xl border-2 border-dashed border-slate-200">
                            <p className="text-slate-400 font-medium mb-3">"{keyword}" 상품 검색 결과가 없습니다.</p>
                            <a
                                href={getSearchLink(mallType, keyword)}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-block bg-white border border-slate-200 px-4 py-2 rounded-xl text-sm font-bold text-slate-700 hover:bg-slate-100 transition-colors shadow-sm"
                            >
                                해당 마트 검색 페이지로 이동
                            </a>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                            {productsByMall[mallType].map(product => (
                                <div key={product.id} className="border border-slate-100 rounded-2xl p-4 hover:shadow-xl hover:-translate-y-1 transition-all group bg-white">
                                    <div className="aspect-square bg-slate-50 rounded-xl mb-4 flex items-center justify-center text-slate-300 group-hover:bg-indigo-50 transition-colors overflow-hidden">
                                        {product.productUrl && product.productUrl.startsWith('http') ? (
                                            <img
                                                src={`https://www.google.com/s2/favicons?domain=${new URL(product.productUrl).hostname}&sz=64`}
                                                className="w-12 h-12 opacity-50"
                                                alt="shop"
                                            />
                                        ) : (
                                            <span className="text-4xl text-slate-200">🛒</span>
                                        )}
                                            <img
                                                src={`https://www.google.com/s2/favicons?domain=${new URL(product.productUrl).hostname}&sz=64`}
                                                className="w-12 h-12 opacity-50"
                                                alt="shop"
                                            />
                                        ) : (
                                            <span className="text-4xl text-slate-200">🛒</span>
                                        )}
                                    </div>
                                    <div className="space-y-1">
                                        <h3 className="font-bold text-slate-800 line-clamp-2 h-10 text-sm">{product.name}</h3>
                                        <p className="text-indigo-600 font-black text-xl">
                                            {product.price.toLocaleString()}원
                                        </p>
                                        <div className="text-[10px] font-black text-slate-400 flex justify-between items-center mt-2 uppercase tracking-tight">
                                            <span className="bg-slate-100 px-2 py-1 rounded">{product.capacity}{product.unit}</span>
                                            {product.productUrl && (
                                                <a
                                                    href={product.productUrl}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="text-indigo-500 hover:underline"
                                                >
                                                    상품 보기
                                                </a>
                                            )}
                                        </div>
                                    </div>
                                    <button
                                        onClick={() => onAddToCart(product, 1)}
                                        className="w-full mt-4 py-3 bg-slate-900 text-white rounded-xl hover:bg-indigo-600 transition-colors font-black text-xs uppercase tracking-widest shadow-lg shadow-gray-200"
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
