/**
 * 쇼핑몰별 검색 페이지 URL을 생성합니다.
 * @param {string} mall - 쇼핑몰 타입 ('COUPANG', 'KURLY', 'NAVER', 'BMART')
 * @param {string} keyword - 검색 키워드
 * @returns {string} 검색 페이지 URL
 */
export function getSearchLink(mall, keyword) {
    const encoded = encodeURIComponent(keyword);
    switch (mall) {
        case 'COUPANG': return `https://www.coupang.com/np/search?q=${encoded}`;
        case 'KURLY': return `https://www.kurly.com/search?searchTerm=${encoded}`;
        case 'NAVER': return `https://search.shopping.naver.com/search/all?query=${encoded}`;
        case 'BMART': return `https://search.shopping.naver.com/search/all?query=B마트+${encoded}`;
        default: return `https://search.shopping.naver.com/search/all?query=${encoded}`;
    }
}
