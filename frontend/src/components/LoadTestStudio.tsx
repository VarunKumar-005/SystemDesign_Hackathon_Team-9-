'use client';

import React, { useState } from 'react';
import {
  Cpu,
  Zap,
  ShieldCheck,
  AlertCircle,
  Play,
  RotateCcw,
  CheckCircle2,
  BarChart3,
  TrendingUp,
  Clock,
  Sparkles,
} from 'lucide-react';
import { LoadSimulationResponse } from '@/types';
import { api } from '@/lib/api';

interface LoadTestStudioProps {
  onRefresh: () => void;
}

export const LoadTestStudio: React.FC<LoadTestStudioProps> = ({ onRefresh }) => {
  const [concurrentRequests, setConcurrentRequests] = useState(1000);
  const [stockToTest, setStockToTest] = useState(100);
  const [running, setRunning] = useState(false);
  const [result, setResult] = useState<LoadSimulationResponse | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const presets = [100, 250, 500, 1000, 2000];

  const handleRunTest = async () => {
    setRunning(true);
    setErrorMessage(null);
    setResult(null);

    try {
      const res = await api.simulateLoad({
        productId: 1,
        availableStock: stockToTest,
        concurrentRequests,
        quantityPerRequest: 1,
      });
      setResult(res);
      onRefresh();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to execute concurrency simulation');
      }
    } finally {
      setRunning(false);
    }
  };

  const handleResetAndRetest = async () => {
    try {
      await api.resetInventory(1, stockToTest);
      onRefresh();
      await handleRunTest();
    } catch (err: unknown) {
      if (err instanceof Error) setErrorMessage(err.message);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 border-b border-white/10 pb-6">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-2 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              <Cpu className="w-5 h-5" />
            </span>
            <h1 className="text-2xl font-black text-white">
              High-Concurrency Load Test Studio
            </h1>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Empirical stress-testing of Redis atomic locking & PostgreSQL conditional updates under massive simultaneous traffic
          </p>
        </div>

        <div className="flex items-center gap-2">
          <span className="px-3 py-1 rounded-full text-xs font-mono font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4" /> ZERO-OVERSELL PROOF
          </span>
        </div>
      </div>

      {errorMessage && (
        <div className="rounded-2xl bg-rose-500/10 border border-rose-500/30 p-4 text-xs text-rose-300 flex items-center gap-2">
          <AlertCircle className="w-4 h-4 flex-shrink-0 text-rose-400" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* Control Configuration Panel */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        <div className="lg:col-span-5 glass-panel rounded-3xl p-6 sm:p-8 border border-white/10 space-y-6">
          <h2 className="text-base font-extrabold text-white flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-amber-400" /> Stress Test Parameters
          </h2>

          {/* Concurrent Requests Selector */}
          <div className="space-y-3">
            <div className="flex justify-between items-center text-xs">
              <label className="font-bold text-slate-300">Concurrent Buyer Threads</label>
              <span className="text-amber-400 font-mono font-bold text-sm">
                {concurrentRequests.toLocaleString()} Threads
              </span>
            </div>

            {/* Slider */}
            <input
              type="range"
              min="50"
              max="2000"
              step="50"
              value={concurrentRequests}
              onChange={(e) => setConcurrentRequests(Number(e.target.value))}
              disabled={running}
              className="w-full accent-amber-400 cursor-pointer h-2 bg-slate-800 rounded-lg"
            />

            {/* Preset Buttons */}
            <div className="flex flex-wrap gap-2 pt-1">
              {presets.map((p) => (
                <button
                  key={p}
                  type="button"
                  onClick={() => setConcurrentRequests(p)}
                  disabled={running}
                  className={`px-2.5 py-1 rounded-lg text-xs font-mono transition ${
                    concurrentRequests === p
                      ? 'bg-amber-500 text-slate-950 font-bold'
                      : 'bg-white/5 text-slate-400 hover:text-white border border-white/10'
                  }`}
                >
                  {p}
                </button>
              ))}
            </div>
          </div>

          {/* Available Stock Input */}
          <div className="space-y-2">
            <div className="flex justify-between items-center text-xs">
              <label className="font-bold text-slate-300">Target Product Stock</label>
              <span className="text-cyan-400 font-mono font-bold text-sm">{stockToTest} Units</span>
            </div>
            <div className="flex gap-2">
              {[50, 100, 200].map((s) => (
                <button
                  key={s}
                  type="button"
                  onClick={() => setStockToTest(s)}
                  disabled={running}
                  className={`flex-1 py-1.5 rounded-lg text-xs font-mono transition ${
                    stockToTest === s
                      ? 'bg-cyan-500 text-slate-950 font-bold'
                      : 'bg-white/5 text-slate-400 hover:text-white border border-white/10'
                  }`}
                >
                  {s} Units
                </button>
              ))}
            </div>
          </div>

          {/* Run Button */}
          <button
            onClick={handleRunTest}
            disabled={running}
            className="w-full py-4 rounded-2xl font-black text-slate-950 bg-gradient-to-r from-emerald-400 via-teal-300 to-emerald-400 hover:from-emerald-300 hover:to-teal-200 shadow-xl shadow-emerald-500/25 flex items-center justify-center gap-3 transition transform hover:-translate-y-0.5 cursor-pointer text-sm"
          >
            {running ? (
              <>
                <div className="w-5 h-5 border-3 border-slate-950 border-t-transparent rounded-full animate-spin" />
                <span>EXECUTING CONCURRENT THREAD BURST...</span>
              </>
            ) : (
              <>
                <Play className="w-4 h-4 fill-slate-950" />
                <span>FIRE CONCURRENT LOAD TEST ({concurrentRequests} REQS)</span>
              </>
            )}
          </button>

          <p className="text-[11px] text-slate-400 text-center">
            Spawns Java virtual threads competing for the exact same DB row & Redis lock.
          </p>
        </div>

        {/* Results Panel */}
        <div className="lg:col-span-7 space-y-6">
          {result ? (
            <div className="glass-panel rounded-3xl p-6 sm:p-8 border border-white/10 space-y-6 animate-in fade-in duration-300">
              {/* Top Proof Banner */}
              <div
                className={`p-5 rounded-2xl border flex items-center justify-between ${
                  result.oversoldQuantity === 0
                    ? 'bg-emerald-950/30 border-emerald-500/40 text-emerald-300 glow-emerald'
                    : 'bg-rose-950/30 border-rose-500/40 text-rose-300'
                }`}
              >
                <div className="flex items-center gap-3">
                  <ShieldCheck className="w-8 h-8 text-emerald-400 flex-shrink-0" />
                  <div>
                    <h3 className="font-extrabold text-white text-base">
                      {result.oversoldQuantity === 0
                        ? '0 OVERSOLD — CONCURRENCY INVARIANT PROVEN!'
                        : 'VIOLATION DETECTED'}
                    </h3>
                    <p className="text-xs text-slate-300 mt-0.5">{result.message}</p>
                  </div>
                </div>

                <div className="text-right font-mono">
                  <div className="text-2xl font-black text-emerald-400">
                    {result.oversoldQuantity}
                  </div>
                  <div className="text-[10px] uppercase text-slate-400">Oversold</div>
                </div>
              </div>

              {/* Statistics Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                <div className="p-3.5 rounded-xl bg-slate-950/70 border border-white/5 text-center font-mono">
                  <div className="text-xs text-slate-400 font-sans">Total Requests</div>
                  <div className="text-xl font-bold text-white mt-1">
                    {result.totalRequests.toLocaleString()}
                  </div>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-950/70 border border-emerald-500/20 text-center font-mono">
                  <div className="text-xs text-emerald-400 font-sans">Won Reservation</div>
                  <div className="text-xl font-bold text-emerald-400 mt-1">
                    {result.successfulReservations}
                  </div>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-950/70 border border-amber-500/20 text-center font-mono">
                  <div className="text-xs text-amber-400 font-sans">Clean 409 Rejection</div>
                  <div className="text-xl font-bold text-amber-400 mt-1">
                    {result.rejectedRequests.toLocaleString()}
                  </div>
                </div>

                <div className="p-3.5 rounded-xl bg-slate-950/70 border border-cyan-500/20 text-center font-mono">
                  <div className="text-xs text-cyan-400 font-sans">Duration</div>
                  <div className="text-xl font-bold text-cyan-400 mt-1">
                    {result.durationMillis}ms
                  </div>
                </div>
              </div>

              {/* Throughput Gauge */}
              <div className="p-4 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-between font-mono text-xs">
                <span className="text-slate-300 font-sans flex items-center gap-2">
                  <TrendingUp className="w-4 h-4 text-emerald-400" /> Throughput Achieved:
                </span>
                <span className="text-lg font-black text-amber-400">
                  {Math.round(result.requestsPerSecond).toLocaleString()} req / sec
                </span>
              </div>

              {/* Repeat Test Button */}
              <div className="pt-2 flex gap-3">
                <button
                  onClick={handleResetAndRetest}
                  disabled={running}
                  className="flex-1 py-3 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-bold text-slate-200 transition flex items-center justify-center gap-2"
                >
                  <RotateCcw className="w-4 h-4" />
                  <span>Reset Stock & Re-run Stress Test</span>
                </button>
              </div>
            </div>
          ) : (
            /* Standby State */
            <div className="glass-panel rounded-3xl p-12 border border-white/10 text-center space-y-4">
              <div className="w-16 h-16 rounded-2xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center justify-center mx-auto">
                <BarChart3 className="w-8 h-8" />
              </div>
              <h3 className="text-lg font-bold text-white">Lab Ready for Stress Testing</h3>
              <p className="text-xs text-slate-400 max-w-md mx-auto leading-relaxed">
                Click &quot;Fire Concurrent Load Test&quot; to unleash hundreds of concurrent requests against the flash sale inventory. The platform uses row-level conditional locking to ensure strict zero overselling.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
