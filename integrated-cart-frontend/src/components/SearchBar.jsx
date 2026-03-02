import { useState, useEffect } from 'react';

/**
 * 레시피 분석을 위한 검색바 컴포넌트.
 * 음식명 또는 URL 입력, 인분 선택, 마트 선택 기능을 포함합니다.
 * @param {Object} props
 * @param {Function} props.onAnalyze - 분석 버튼 클릭 시 호출되는 콜백
 * @param {boolean} props.isLoading - 분석 진행 중 여부
 * @returns {JSX.Element} 검색바 UI
 */
export default function SearchBar({ onAnalyze, isLoading }) {
    const [inputUrl, setInputUrl] = useState('');
    const [portion, setPortion] = useState(1);
    const [malls, setMalls] = useState(['ALL']);
    const [selectedModel, setSelectedModel] = useState('gemini-1.5-flash');
    const [models, setModels] = useState([]);

    useEffect(() => {
        const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';
        fetch(`${API_BASE}/ai/models`)
            .then(res => {
                if (!res.ok) throw new Error("API Not Found");
                return res.json();
            })
        fetch('http://localhost:8080/api/v1/ai/models')
            .then(res => {
                if (!res.ok) throw new Error("API Not Found");
                return res.json();
            })
            .then(data => {
                if (Array.isArray(data) && data.length > 0) {
                    setModels(data);
                } else {
                    setModels(['gemini-1.5-flash', 'gemini-pro']);
                }
            })
            .catch(() => setModels(['gemini-1.5-flash', 'gemini-pro']));
    }, []);
        fetch('http://localhost:8080/api/v1/ai/models')
            .then(res => res.json())
            .then(data => setModels(data))
            .catch(() => setModels(['gemini-1.5-flash', 'gemini-pro']));
    }, []);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (inputUrl.trim()) {
            onAnalyze(inputUrl, portion, malls, selectedModel);
        }
    };

    const toggleMall = (mall) => {
        if (mall === 'ALL') {
            setMalls(['ALL']);
            return;
        }
        setMalls(prev => {
            const filtered = prev.filter(m => m !== 'ALL');
            if (filtered.includes(mall)) {
                const next = filtered.filter(m => m !== mall);
                return next.length === 0 ? ['ALL'] : next;
            } else {
                return [...filtered, mall];
            }
        });
    };

    return (
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-center gap-6">
            <h2 className="text-2xl font-bold text-gray-800 w-full text-center">
                레시피 분석 및 최저가 재료 찾기
            </h2>

            <form onSubmit={handleSubmit} className="w-full max-w-3xl space-y-4">
                <div className="relative">
                    <input
                        type="text"
                        className="w-full pl-6 pr-32 py-4 rounded-xl bg-gray-50 border-transparent focus:bg-white focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all text-lg text-gray-700 outline-none shadow-sm"
                        placeholder="음식명(예: 제육볶음) 또는 유튜브 URL 입력"
                        value={inputUrl}
                        onChange={(e) => setInputUrl(e.target.value)}
                        disabled={isLoading}
                    />
                    <button
                        type="submit"
                        disabled={isLoading || !inputUrl.trim()}
                        className="absolute right-2 top-2 bottom-2 px-8 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg font-bold transition-all disabled:opacity-50"
                    >
                        {isLoading ? '분석 중...' : '재료 찾기'}
                    </button>
                </div>

                <div className="flex flex-col md:flex-row gap-6 items-start md:items-center justify-between p-4 bg-gray-50 rounded-xl">
                    <div className="flex items-center gap-4">
                        <span className="text-sm font-bold text-gray-600">AI 모델:</span>
                        <select 
                            value={selectedModel} 
                            onChange={(e) => setSelectedModel(e.target.value)}
                            className="bg-white border border-gray-200 rounded-lg px-3 py-2 text-sm font-medium outline-none focus:ring-2 focus:ring-indigo-200 transition-all"
                        >
                            {models.map(m => <option key={m} value={m}>{m}</option>)}
                        </select>
                    </div>

                    <div className="flex items-center gap-4">
                        <span className="text-sm font-bold text-gray-600">인분 선택:</span>
                        <div className="flex gap-2">
                            {[1, 2, 4].map(n => (
                                <button
                                    key={n}
                                    type="button"
                                    onClick={() => setPortion(n)}
                                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-all ${portion === n ? 'bg-indigo-600 text-white' : 'bg-white text-gray-500 hover:bg-indigo-50'}`}
                                >
                                    {n}인분
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="flex items-center gap-4">
                        <span className="text-sm font-bold text-gray-600">마트 선택:</span>
                        <div className="flex flex-wrap gap-2">
                            {[
                                { id: 'ALL', name: '통합' },
                                { id: 'KURLY', name: '컬리' },
                                { id: 'NAVER', name: '네이버' },
                                { id: 'BMART', name: 'B마트' },
                                { id: 'COUPANG', name: '쿠팡' }
                            ].map(mall => (
                                <button
                                    key={mall.id}
                                    type="button"
                                    onClick={() => toggleMall(mall.id)}
                                    className={`px-3 py-2 rounded-lg text-xs font-bold transition-all border ${malls.includes(mall.id) ? 'bg-slate-800 text-white border-slate-800' : 'bg-white text-gray-500 border-gray-200 hover:border-indigo-300'}`}
                                >
                                    {mall.name}
                                </button>
                            ))}
                        </div>
                    </div>
                </div>
            </form>

            <div className="flex flex-wrap justify-center gap-3">
                <span className="px-4 py-2 bg-emerald-50 text-emerald-700 rounded-full text-xs font-bold flex items-center gap-1">
                    <span className="w-2 h-2 bg-emerald-500 rounded-full"></span> 실시간 최저가 반영
                </span>
                <span className="px-4 py-2 bg-blue-50 text-blue-700 rounded-full text-xs font-bold flex items-center gap-1">
                    <span className="w-2 h-2 bg-blue-500 rounded-full"></span> 정밀 레시피 분석
                </span>
                <span className="px-4 py-2 bg-purple-50 text-purple-700 rounded-full text-xs font-bold flex items-center gap-1">
                    <span className="w-2 h-2 bg-purple-500 rounded-full"></span> 앱 전용 마켓 우회 지원
                </span>
            </div>
        </div>
    );
}
