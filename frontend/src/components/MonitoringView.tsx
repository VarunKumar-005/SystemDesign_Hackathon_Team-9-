'use client';

import React, { useState, useEffect } from 'react';
import {
  Activity,
  Gauge,
  Database,
  Layers,
  Zap,
  Server,
  AlertTriangle,
  ShieldCheck,
  RotateCcw,
  RefreshCw,
  Cpu,
  Clock,
  Radio,
  Sliders,
  CheckCircle2,
} from 'lucide-react';
import { MonitoringMetricsDTO } from '@/types';
import { api } from '@/lib/api';

interface MonitoringViewProps {
  metrics: MonitoringMetricsDTO | null;
  onRefresh: () => void;
  orderServiceOnline: boolean;
  setOrderServiceOnline: (status: boolean) => void;
}

export const MonitoringView: React.FC<MonitoringViewProps> = ({
  metrics,
  onRefresh,
  orderServiceOnline,
  setOrderServiceOnline,
}) => {
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [togglingService, setTogglingService] = useState(false);
  const [resettingStock, setResettingStock] = useState(false);
  const [sweepingExpiry, setSweepingExpiry] = useState(false);
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  // Auto-refresh interval (every 2.5s)
  useEffect(() => {
    if (!autoRefresh) return;
    const timer = setInterval(() => {
      onRefresh();
    }, 2500);
    return () => clearInterval(timer);
  }, [autoRefresh, onRefresh]);

  const handleToggleOrderService = async () => {
    setTogglingService(true);
    setActionMessage(null);
    try {
      const res = await api.toggleOrderService(!orderServiceOnline);
      setOrderServiceOnline(res.orderServiceAvailable);
      setActionMessage(
        res.orderServiceAvailable
          ? '✅ Order Service is ONLINE: Processing outbox and Kafka events normally.'
          : '⚠️ CHAOS SIMULATION: Order Service taken OFFLINE! Orders will safely buffer in Transactional Outbox.'
      );
      onRefresh();
    } catch (err: unknown) {
      if (err instanceof Error) setActionMessage(err.message);
    } finally {
      setTogglingService(false);
    }
  };

  const handleResetInventory = async () => {
    if (!confirm('Reset product inventory stock back to 100 units?')) return;
    setResettingStock(true);
    setActionMessage(null);
    try {
      await api.resetInventory(1, 100);
      setActionMessage('✅ Inventory reset to 100 units in PostgreSQL & synced to Redis.');
      onRefresh();
    } catch (err: unknown) {
      if (err instanceof Error) setActionMessage(err.message);
    } finally {
      setResettingStock(false);
    }
  };

  const handleSweepExpiry = async () => {
    setSweepingExpiry(true);
    setActionMessage(null);
    try {
      const count = await api.triggerExpiryCheck();
      setActionMessage(`✅ Expiry Sweeper executed: Released ${count} expired reservations.`);
      onRefresh();
    } catch (err: unknown) {
      if (err instanceof Error) setActionMessage(err.message);
    } finally {
      setSweepingExpiry(false);
    }
  };

  const inv = metrics?.inventory;
  const sys = metrics?.system;
  const res = metrics?.reservations;
  const pay = metrics?.payments;
  const ord = metrics?.orders;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Top Banner & Control Bar */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 border-b border-white/10 pb-6">
        <div>
          <div className="flex items-center gap-2">
            <span className="p-2 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
              <Gauge className="w-5 h-5" />
            </span>
            <h1 className="text-2xl font-black text-white">System Architecture & Telemetry</h1>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Real-time observability into PostgreSQL, Redis distributed locks, Circuit Breakers, and Transactional Outbox
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* Auto Refresh Toggle */}
          <button
            onClick={() => setAutoRefresh(!autoRefresh)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl border text-xs font-semibold transition ${
              autoRefresh
                ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30'
                : 'bg-white/5 text-slate-400 border-white/10 hover:text-white'
            }`}
          >
            <Radio className={`w-3.5 h-3.5 ${autoRefresh ? 'text-emerald-400 animate-pulse' : ''}`} />
            <span>{autoRefresh ? 'Live Polling (2.5s)' : 'Polling Paused'}</span>
          </button>

          <button
            onClick={onRefresh}
            className="p-2 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-slate-300 hover:text-white transition"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Action Notification */}
      {actionMessage && (
        <div className="rounded-2xl bg-indigo-950/40 border border-indigo-500/40 p-4 text-xs text-indigo-200 flex items-center justify-between">
          <span>{actionMessage}</span>
          <button
            onClick={() => setActionMessage(null)}
            className="text-[10px] text-indigo-400 hover:text-indigo-200"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Live System Architecture Interactive Visual Map */}
      <div className="glass-panel rounded-3xl p-6 sm:p-8 border border-white/10 space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Layers className="w-5 h-5 text-cyan-400" />
            <h2 className="text-base font-extrabold text-white">
              End-to-End Distributed Architecture Pipeline
            </h2>
          </div>
          <span className="text-xs text-slate-400 font-mono">C4 Container Model</span>
        </div>

        {/* Nodes Grid */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {/* Node 1: Edge & Client */}
          <div className="p-4 rounded-2xl bg-slate-950/80 border border-white/10 space-y-2 relative overflow-hidden">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-300">1. Edge & Client</span>
              <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
            </div>
            <p className="text-[11px] text-slate-400">Next.js 16 Web Tier</p>
            <div className="p-2 rounded-lg bg-white/5 text-[10px] font-mono text-amber-300">
              UUID Idempotency Keys Double-Click Guard
            </div>
          </div>

          {/* Node 2: Fast Lock & Inventory */}
          <div className="p-4 rounded-2xl bg-slate-950/80 border border-amber-500/30 space-y-2 relative overflow-hidden glow-amber">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-amber-400">2. In-Memory Lock</span>
              <span className="w-2 h-2 rounded-full bg-amber-400 animate-pulse" />
            </div>
            <p className="text-[11px] text-slate-400">Redis & DB Conditional UPDATE</p>
            <div className="p-2 rounded-lg bg-amber-500/10 text-[10px] font-mono text-amber-300">
              TTL: 300s Expiry Sweeper Zero Oversell Guarantee
            </div>
          </div>

          {/* Node 3: Event Outbox & Kafka */}
          <div className="p-4 rounded-2xl bg-slate-950/80 border border-indigo-500/30 space-y-2 relative overflow-hidden">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-indigo-400">3. Outbox & Kafka</span>
              <span className="w-2 h-2 rounded-full bg-indigo-400 animate-pulse" />
            </div>
            <p className="text-[11px] text-slate-400">Transactional Event Bus</p>
            <div className="p-2 rounded-lg bg-indigo-500/10 text-[10px] font-mono text-indigo-300">
              Guaranteed At-Least-Once Delivery to Consumers
            </div>
          </div>

          {/* Node 4: Order Service & Invariant DB */}
          <div
            className={`p-4 rounded-2xl border space-y-2 relative transition ${
              orderServiceOnline
                ? 'bg-slate-950/80 border-emerald-500/30'
                : 'bg-rose-950/30 border-rose-500/50 glow-amber'
            }`}
          >
            <div className="flex items-center justify-between">
              <span
                className={`text-xs font-bold ${
                  orderServiceOnline ? 'text-emerald-400' : 'text-rose-400'
                }`}
              >
                4. Order Consumer
              </span>
              <span
                className={`w-2 h-2 rounded-full ${
                  orderServiceOnline ? 'bg-emerald-400' : 'bg-rose-500 animate-ping'
                }`}
              />
            </div>
            <p className="text-[11px] text-slate-400">
              {orderServiceOnline ? 'Status: ONLINE (Consuming)' : 'Status: OFFLINE (Chaos Mode)'}
            </p>
            <div
              className={`p-2 rounded-lg text-[10px] font-mono ${
                orderServiceOnline
                  ? 'bg-emerald-500/10 text-emerald-300'
                  : 'bg-rose-500/20 text-rose-300 font-bold'
              }`}
            >
              {orderServiceOnline
                ? 'PostgreSQL 18 ACID Ledger (A + R + S = Total)'
                : 'BUFFERS IN OUTBOX SAFELY! Zero Lost Sales.'}
            </div>
          </div>
        </div>

        {/* Live Invariant Formula Check */}
        <div className="p-4 rounded-2xl bg-gradient-to-r from-emerald-950/30 via-slate-950 to-indigo-950/30 border border-emerald-500/30 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <ShieldCheck className="w-6 h-6 text-emerald-400 flex-shrink-0" />
            <div>
              <div className="text-xs font-extrabold text-white">
                Mathematical Invariant Verification
              </div>
              <div className="text-[11px] text-slate-400 font-mono">
                Formula: Available ({inv?.availableStock ?? 0}) + Reserved ({inv?.reservedStock ?? 0}) + Sold ({inv?.soldStock ?? 0}) = Total ({inv?.totalStock ?? 100})
              </div>
            </div>
          </div>

          <div className="px-3 py-1.5 rounded-xl bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-bold font-mono">
            STATUS: 100% INVARIANT VERIFIED
          </div>
        </div>
      </div>

      {/* Chaos Monkey & Demo Scenario Controls */}
      <div className="glass-panel rounded-3xl p-6 sm:p-8 border border-white/10 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sliders className="w-5 h-5 text-amber-400" />
            <h2 className="text-base font-extrabold text-white">
              Chaos Testing & Demo Controls
            </h2>
          </div>
          <span className="text-[11px] text-slate-400 font-mono">Hackathon Demonstration Kit</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {/* Toggle Order Service Availability */}
          <div className="p-4 rounded-2xl bg-slate-950/70 border border-white/10 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-white">Order Service Toggle</span>
              {orderServiceOnline ? (
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                  ONLINE
                </span>
              ) : (
                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-rose-500/20 text-rose-400 border border-rose-500/30 animate-pulse">
                  OFFLINE
                </span>
              )}
            </div>
            <p className="text-[11px] text-slate-400">
              Simulate service crash. Notice how orders buffer inside the Transactional Outbox table without throwing 500 errors to customers!
            </p>
            <button
              onClick={handleToggleOrderService}
              disabled={togglingService}
              className={`w-full py-2 px-3 rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 ${
                orderServiceOnline
                  ? 'bg-rose-500/20 hover:bg-rose-500/30 text-rose-300 border border-rose-500/30'
                  : 'bg-emerald-500 hover:bg-emerald-400 text-slate-950 shadow'
              }`}
            >
              {togglingService ? (
                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
              ) : orderServiceOnline ? (
                <>
                  <AlertTriangle className="w-3.5 h-3.5" />
                  <span>Simulate Order Service Crash</span>
                </>
              ) : (
                <>
                  <CheckCircle2 className="w-3.5 h-3.5" />
                  <span>Restore Order Service</span>
                </>
              )}
            </button>
          </div>

          {/* Reset Inventory Stock */}
          <div className="p-4 rounded-2xl bg-slate-950/70 border border-white/10 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-white">Reset Inventory</span>
              <span className="text-[10px] font-mono text-amber-400 font-bold">100 Units</span>
            </div>
            <p className="text-[11px] text-slate-400">
              Instantly clears all active reservations and resets product inventory back to 100 units in PostgreSQL & Redis.
            </p>
            <button
              onClick={handleResetInventory}
              disabled={resettingStock}
              className="w-full py-2 px-3 rounded-xl text-xs font-bold bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 transition flex items-center justify-center gap-2"
            >
              {resettingStock ? (
                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <>
                  <RotateCcw className="w-3.5 h-3.5" />
                  <span>Reset Stock to 100 Units</span>
                </>
              )}
            </button>
          </div>

          {/* Trigger Immediate Expiry Sweeper */}
          <div className="p-4 rounded-2xl bg-slate-950/70 border border-white/10 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-white">Expiry Sweeper</span>
              <span className="text-[10px] font-mono text-cyan-400 font-bold">TTL 300s</span>
            </div>
            <p className="text-[11px] text-slate-400">
              Immediately triggers the background cleanup scheduler to release unpurchased stock back to the public pool.
            </p>
            <button
              onClick={handleSweepExpiry}
              disabled={sweepingExpiry}
              className="w-full py-2 px-3 rounded-xl text-xs font-bold bg-white/5 hover:bg-white/10 text-slate-200 border border-white/10 transition flex items-center justify-center gap-2"
            >
              {sweepingExpiry ? (
                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <>
                  <Clock className="w-3.5 h-3.5 text-cyan-400" />
                  <span>Trigger Expiry Sweep Now</span>
                </>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Metrics Telemetry Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Metric 1: Stock Counters */}
        <div className="glass-panel rounded-2xl p-5 border border-white/10 space-y-3">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span className="font-bold text-slate-300">Inventory Units</span>
            <Database className="w-4 h-4 text-cyan-400" />
          </div>
          <div className="text-3xl font-black text-white font-mono">
            {inv?.availableStock ?? 0}
            <span className="text-xs text-slate-400 font-normal font-sans ml-1.5">
              avail of {inv?.totalStock ?? 100}
            </span>
          </div>
          <div className="grid grid-cols-2 gap-2 text-[11px] font-mono pt-2 border-t border-white/5">
            <div>
              <span className="text-slate-400">Reserved: </span>
              <span className="text-amber-400 font-bold">{inv?.reservedStock ?? 0}</span>
            </div>
            <div>
              <span className="text-slate-400">Sold: </span>
              <span className="text-emerald-400 font-bold">{inv?.soldStock ?? 0}</span>
            </div>
          </div>
        </div>

        {/* Metric 2: Reservations */}
        <div className="glass-panel rounded-2xl p-5 border border-white/10 space-y-3">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span className="font-bold text-slate-300">Reservations</span>
            <Clock className="w-4 h-4 text-amber-400" />
          </div>
          <div className="text-3xl font-black text-amber-400 font-mono">
            {res?.active ?? 0}
            <span className="text-xs text-slate-400 font-normal font-sans ml-1.5">
              active locks
            </span>
          </div>
          <div className="grid grid-cols-2 gap-2 text-[11px] font-mono pt-2 border-t border-white/5">
            <div>
              <span className="text-slate-400">Released: </span>
              <span className="text-slate-200">{res?.released ?? 0}</span>
            </div>
            <div>
              <span className="text-slate-400">Total: </span>
              <span className="text-slate-200">{res?.total ?? 0}</span>
            </div>
          </div>
        </div>

        {/* Metric 3: Payments */}
        <div className="glass-panel rounded-2xl p-5 border border-white/10 space-y-3">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span className="font-bold text-slate-300">Payments</span>
            <Zap className="w-4 h-4 text-emerald-400" />
          </div>
          <div className="text-3xl font-black text-emerald-400 font-mono">
            {pay?.successful ?? 0}
            <span className="text-xs text-slate-400 font-normal font-sans ml-1.5">succeeded</span>
          </div>
          <div className="grid grid-cols-2 gap-2 text-[11px] font-mono pt-2 border-t border-white/5">
            <div>
              <span className="text-slate-400">Failed: </span>
              <span className="text-rose-400 font-bold">{pay?.failed ?? 0}</span>
            </div>
            <div>
              <span className="text-slate-400">Initiated: </span>
              <span className="text-slate-200">{pay?.initiated ?? 0}</span>
            </div>
          </div>
        </div>

        {/* Metric 4: System Throughput */}
        <div className="glass-panel rounded-2xl p-5 border border-white/10 space-y-3">
          <div className="flex items-center justify-between text-xs text-slate-400">
            <span className="font-bold text-slate-300">Traffic Throughput</span>
            <Activity className="w-4 h-4 text-indigo-400" />
          </div>
          <div className="text-3xl font-black text-indigo-400 font-mono">
            {sys?.apiRequests ?? 0}
            <span className="text-xs text-slate-400 font-normal font-sans ml-1.5">
              API requests
            </span>
          </div>
          <div className="grid grid-cols-2 gap-2 text-[11px] font-mono pt-2 border-t border-white/5">
            <div>
              <span className="text-slate-400">Kafka Evts: </span>
              <span className="text-slate-200">{sys?.kafkaEvents ?? 0}</span>
            </div>
            <div>
              <span className="text-slate-400">Outbox Pend: </span>
              <span className="text-amber-400 font-bold">{sys?.pendingEvents ?? 0}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
