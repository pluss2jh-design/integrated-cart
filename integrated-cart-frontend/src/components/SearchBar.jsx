import { useState } from 'react';

export default function SearchBar({ onAnalyze, isLoading }) {
    const [inputUrl, setInputUrl] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        if (inputUrl.trim()) {
            onAnalyze(inputUrl);
        }
    };

    return (
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-center">
            <h2 className="text-2xl font-bold text-gray-800 mb-6 w-full text-center">
                레시피 영상 URL이나 음식명을 입력하세요
            </h2>
            <form onSubmit={handleSubmit} className="w-full max-w-2xl relative">
                <input
                    type="text"
                    className="w-full pl-6 pr-32 py-4 rounded-xl bg-gray-50 border-transparent focus:bg-white focus:border-primary-500 focus:ring-2 focus:ring-primary-200 transition-all text-gray-700 outline-none shadow-sm"
                    placeholder="예: https://youtube.com/... 또는 제육볶음"
                    value={inputUrl}
                    onChange={(e) => setInputUrl(e.target.value)}
                    disabled={isLoading}
                />
                <button
                    type="submit"
                    disabled={isLoading || !inputUrl.trim()}
                    className="absolute right-2 top-2 bottom-2 px-6 bg-primary-600 hover:bg-primary-700 text-white rounded-lg font-medium transition-colors disabled:opacity-50"
                >
                    {isLoading ? '분석 중...' : '재료 찾기'}
                </button>
            </form>
            <div className="mt-6 flex flex-wrap justify-center gap-3">
                <span className="px-4 py-2 bg-slate-100 text-slate-600 rounded-full text-sm font-medium">✨ 1인분 추천</span>
                <span className="px-4 py-2 bg-slate-100 text-slate-600 rounded-full text-sm font-medium">💰 최저가 탐색</span>
                <span className="px-4 py-2 bg-slate-100 text-slate-600 rounded-full text-sm font-medium">🥗 저당 옵션</span>
            </div>
        </div>
    );
}
