// 콘텐츠 스크립트에서 보내는 검색 요청을 수신
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.action === "scrape_coupang") {
        fetch(`https://www.coupang.com/np/search?q=${encodeURIComponent(request.keyword)}`)
            .then(response => response.text())
            .then(html => sendResponse({ success: true, html: html }))
            .catch(error => sendResponse({ success: false, error: error.message }));
        return true; // 비동기 fetch를 위해 메시지 채널 유지
    }
});
