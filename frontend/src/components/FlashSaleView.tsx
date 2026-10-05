'use client';

import React, { useState, useEffect } from 'react';
import {
  Zap,
  Clock,
  Shield,
  CheckCircle2,
  AlertCircle,
  ArrowRight,
  RotateCcw,
  Sparkles,
  Lock,
  Layers,
} from 'lucide-react';
import { ProductDetailDTO, ReservationResponse, UserSession } from '@/types';
import { api, generateIdempotencyKey } from '@/lib/api';

interface FlashSaleViewProps {
  product: ProductDetailDTO | null;
  activeReservation: ReservationResponse | null;
  setActiveReservation: (res: ReservationResponse | null) => void;
  onOpenCheckout: () => void;
  userSession: UserSession;
  onRefresh: () => void;
}

export const FlashSaleView: React.FC<FlashSaleViewProps> = ({
  product,
  activeReservation,
  setActiveReservation,
  onOpenCheckout,
  userSession,
  onRefresh,
}) => {
  const [reserving, setReserving] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [remainingTime, setRemainingTime] = useState<number>(0);
  const [countdown, setCountdown] = useState({ hours: 4, minutes: 28, seconds: 15 });

  // Sale countdown simulator
  useEffect(() => {
    const timer = setInterval(() => {
      setCountdown((prev) => {
        if (prev.seconds > 0) return { ...prev, seconds: prev.seconds - 1 };
        if (prev.minutes > 0) return { ...prev, minutes: 59, seconds: 59 };
        if (prev.hours > 0) return { ...prev, hours: prev.hours - 1, minutes: 59, seconds: 59 };
        return prev;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  // Reservation countdown ticker
  useEffect(() => {
    if (!activeReservation) {
      setRemainingTime(0);
      return;
    }

    const expiresAt = new Date(activeReservation.expiresAt).getTime();
    const updateRemaining = () => {
      const diff = Math.max(0, Math.floor((expiresAt - Date.now()) / 1000));
      setRemainingTime(diff);
      if (diff === 0) {
        setActiveReservation(null);
        onRefresh();
      }
    };

    updateRemaining();
    const interval = setInterval(updateRemaining, 1000);
    return () => clearInterval(interval);
  }, [activeReservation, setActiveReservation, onRefresh]);

  const handleReserve = async () => {
    if (!product) return;
    setReserving(true);
    setErrorMessage(null);

    try {
      const idempotencyKey = generateIdempotencyKey('res');
      const res = await api.createReservation({
        productId: product.id,
        customerId: userSession.customerId,
        quantity: 1,
        idempotencyKey,
      });
      setActiveReservation(res);
      onRefresh();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMessage(err.message);
      } else {
        setErrorMessage('Failed to reserve stock. The item may be sold out.');
      }
    } finally {
      setReserving(false);
    }
  };

  const handleCancelReservation = async () => {
    if (!activeReservation) return;
    setCancelling(true);
    try {
      await api.cancelReservation(activeReservation.reservationId);
      setActiveReservation(null);
      onRefresh();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMessage(err.message);
      }
    } finally {
      setCancelling(false);
    }
  };

  if (!product) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[500px] text-slate-400">
        <div className="w-12 h-12 border-4 border-amber-400 border-t-transparent rounded-full animate-spin mb-4" />
        <p className="text-sm font-medium">Connecting to SaleStorm High-Scale Engine...</p>
      </div>
    );
  }

  const stockPercentage = Math.round((product.availableStock / product.totalStock) * 100);
  const isOutOfStock = product.availableStock <= 0;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Flash Sale Top Alert Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-amber-500/20 via-orange-500/10 to-transparent border border-amber-500/30 p-4 sm:p-6 backdrop-blur-md">
        <div className="flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span className="flex h-3 w-3 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75" />
              <span className="relative inline-flex rounded-full h-3 w-3 bg-amber-500" />
            </span>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-lg font-extrabold text-white tracking-wide">
                  FLASH SALE LIVE NOW
                </h2>
                <span className="px-2 py-0.5 rounded text-[11px] font-bold bg-amber-500 text-slate-950 uppercase">
                  50% OFF
                </span>
              </div>
              <p className="text-xs text-slate-400">
                High-concurrency atomic reservation lock active. Maximum 1 unit per customer.
              </p>
            </div>
          </div>

          {/* Countdown Clock */}
          <div className="flex items-center gap-2 bg-slate-950/70 px-4 py-2 rounded-xl border border-white/10 font-mono text-sm">
            <Clock className="w-4 h-4 text-amber-400" />
            <span className="text-slate-400 text-xs uppercase font-sans mr-1">Sale Ends In:</span>
            <span className="text-amber-400 font-bold">
              {String(countdown.hours).padStart(2, '0')}h :{' '}
              {String(countdown.minutes).padStart(2, '0')}m :{' '}
              {String(countdown.seconds).padStart(2, '0')}s
            </span>
          </div>
        </div>
      </div>

      {/* Active Reservation Banner (if reserved) */}
      {activeReservation && (
        <div className="rounded-2xl bg-gradient-to-r from-emerald-500/20 via-slate-900 to-emerald-950/30 border border-emerald-500/40 p-5 glow-emerald">
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
            <div className="flex items-start gap-3">
              <div className="p-2.5 rounded-xl bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                <Lock className="w-6 h-6" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <h3 className="font-bold text-white text-base">
                    Stock Reserved Exclusively For You!
                  </h3>
                  <span className="px-2 py-0.5 rounded-full text-[10px] font-mono font-bold bg-emerald-500/30 text-emerald-300 border border-emerald-500/40">
                    RESERVED
                  </span>
                </div>
                <p className="text-xs text-slate-300 mt-0.5">
                  Reservation #{activeReservation.reservationId} • Held in Redis memory with atomic lock.
                </p>
              </div>
            </div>

            <div className="flex items-center gap-4 w-full sm:w-auto justify-between sm:justify-end">
              {/* Timer Badge */}
              <div className="flex items-center gap-2 bg-slate-950/80 px-3.5 py-1.5 rounded-lg border border-emerald-500/30 font-mono">
                <Clock className="w-4 h-4 text-emerald-400 animate-spin" />
                <span className="text-xs text-slate-400">Lock TTL:</span>
                <span className="text-emerald-400 font-extrabold text-sm">
                  {Math.floor(remainingTime / 60)}:
                  {String(remainingTime % 60).padStart(2, '0')}
                </span>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center gap-2">
                <button
                  onClick={handleCancelReservation}
                  disabled={cancelling}
                  title="Release stock back into the pool"
                  className="px-3 py-2 rounded-lg text-xs font-semibold text-slate-400 hover:text-white bg-white/5 hover:bg-white/10 border border-white/10 transition"
                >
                  {cancelling ? 'Releasing...' : 'Release'}
                </button>
                <button
                  onClick={onOpenCheckout}
                  className="flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-bold bg-emerald-500 hover:bg-emerald-400 text-slate-950 shadow-md transition"
                >
                  <span>Checkout Now</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Error Message Toast */}
      {errorMessage && (
        <div className="rounded-xl bg-rose-500/10 border border-rose-500/30 p-4 flex items-center gap-3 text-rose-300 text-sm">
          <AlertCircle className="w-5 h-5 flex-shrink-0 text-rose-400" />
          <div className="flex-1">{errorMessage}</div>
          <button
            onClick={() => setErrorMessage(null)}
            className="text-xs text-rose-400 hover:text-rose-200"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Main Flash Deal Showcase */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Left Column: Product Visual Showcase */}
        <div className="lg:col-span-6 space-y-4">
          <div className="relative rounded-3xl overflow-hidden glass-panel border border-white/10 bg-slate-900 group">
            <div className="aspect-[4/3] w-full overflow-hidden relative">
              {/* Product Image */}
              <img
                src={product.imageUrl}
                alt={product.name}
                className="w-full h-full object-cover object-center group-hover:scale-105 transition-transform duration-500"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/20 to-transparent" />
            </div>

            {/* Badges Over Image */}
            <div className="absolute top-4 left-4 flex gap-2">
              <span className="px-3 py-1 rounded-full text-xs font-extrabold bg-amber-500 text-slate-950 shadow-lg flex items-center gap-1.5">
                <Zap className="w-3.5 h-3.5 fill-slate-950" />
                FLASH DEAL
              </span>
              <span className="px-3 py-1 rounded-full text-xs font-semibold bg-slate-900/90 text-slate-200 border border-white/10 backdrop-blur-md">
                Limited Stock (100 Max)
              </span>
            </div>

            <div className="absolute bottom-4 left-4 right-4 flex items-end justify-between">
              <div>
                <p className="text-xs uppercase tracking-wider text-amber-400 font-bold">
                  Featured Product
                </p>
                <h3 className="text-xl font-bold text-white">{product.name}</h3>
              </div>
              <div className="text-right">
                <span className="text-xs text-slate-400 line-through">
                  ₹{product.originalPrice.toLocaleString('en-IN')}
                </span>
                <div className="text-2xl font-black text-amber-400">
                  ₹{product.salePrice.toLocaleString('en-IN')}
                </div>
              </div>
            </div>
          </div>

          {/* High-Scale Architectural Highlights */}
          <div className="grid grid-cols-3 gap-3">
            <div className="glass-panel rounded-xl p-3 border border-white/5 text-center">
              <Shield className="w-5 h-5 text-amber-400 mx-auto mb-1" />
              <div className="text-[11px] font-bold text-slate-200">Atomic Locks</div>
              <div className="text-[10px] text-slate-400">Zero Overselling</div>
            </div>
            <div className="glass-panel rounded-xl p-3 border border-white/5 text-center">
              <Clock className="w-5 h-5 text-cyan-400 mx-auto mb-1" />
              <div className="text-[11px] font-bold text-slate-200">5-Min TTL</div>
              <div className="text-[10px] text-slate-400">Auto Expiry Sweeper</div>
            </div>
            <div className="glass-panel rounded-xl p-3 border border-white/5 text-center">
              <Layers className="w-5 h-5 text-indigo-400 mx-auto mb-1" />
              <div className="text-[11px] font-bold text-slate-200">Outbox Pattern</div>
              <div className="text-[10px] text-slate-400">Kafka Decoupling</div>
            </div>
          </div>
        </div>

        {/* Right Column: Reservation & Live Inventory Telemetry */}
        <div className="lg:col-span-6 space-y-6">
          <div className="glass-panel rounded-3xl p-6 sm:p-8 border border-white/10 space-y-6">
            {/* Header */}
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-amber-400 uppercase tracking-wider flex items-center gap-1.5">
                  <Sparkles className="w-3.5 h-3.5" /> Special Hackathon Showcase
                </span>
                <span className="text-xs text-slate-400 font-mono">
                  ID: #{product.id} • SKU-SALESTORM-01
                </span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
                {product.name}
              </h1>
              <p className="text-sm text-slate-300 mt-2 leading-relaxed">
                {product.description}
              </p>
            </div>

            {/* Price Box */}
            <div className="flex items-baseline gap-4 p-4 rounded-2xl bg-white/5 border border-white/10">
              <div className="text-3xl sm:text-4xl font-black text-amber-400">
                ₹{product.salePrice.toLocaleString('en-IN')}
              </div>
              <div className="text-lg text-slate-400 line-through">
                ₹{product.originalPrice.toLocaleString('en-IN')}
              </div>
              <div className="ml-auto px-2.5 py-1 rounded-lg bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 text-xs font-extrabold">
                SAVE ₹{(product.originalPrice - product.salePrice).toLocaleString('en-IN')}
              </div>
            </div>

            {/* Real-Time Inventory Stock Gauge */}
            <div className="space-y-3 p-5 rounded-2xl bg-slate-950/60 border border-white/10">
              <div className="flex items-center justify-between text-xs">
                <span className="font-bold text-slate-200 flex items-center gap-2">
                  <span className={`w-2 h-2 rounded-full ${isOutOfStock ? 'bg-rose-500' : 'bg-emerald-400'} animate-pulse`} />
                  Real-Time Stock Gauge
                </span>
                <span className="font-mono text-slate-400">
                  <span className={`font-bold ${isOutOfStock ? 'text-rose-400' : 'text-emerald-400'}`}>
                    {product.availableStock}
                  </span>{' '}
                  / {product.totalStock} Available
                </span>
              </div>

              {/* Multi-Segment Stock Bar */}
              <div className="h-3 w-full bg-slate-800 rounded-full overflow-hidden flex shadow-inner">
                {/* Available */}
                <div
                  style={{ width: `${(product.availableStock / product.totalStock) * 100}%` }}
                  className="bg-gradient-to-r from-emerald-500 to-cyan-400 transition-all duration-500"
                  title={`Available: ${product.availableStock}`}
                />
                {/* Reserved */}
                <div
                  style={{ width: `${(product.reservedStock / product.totalStock) * 100}%` }}
                  className="bg-amber-400 transition-all duration-500"
                  title={`Reserved: ${product.reservedStock}`}
                />
                {/* Sold */}
                <div
                  style={{ width: `${(product.soldStock / product.totalStock) * 100}%` }}
                  className="bg-indigo-600 transition-all duration-500"
                  title={`Sold: ${product.soldStock}`}
                />
              </div>

              {/* Stock Breakdown Legend */}
              <div className="grid grid-cols-3 gap-2 pt-1 text-center font-mono text-[11px]">
                <div className="p-2 rounded-lg bg-emerald-950/30 border border-emerald-500/20">
                  <div className="text-emerald-400 font-extrabold text-sm">{product.availableStock}</div>
                  <div className="text-slate-400 text-[10px] uppercase font-sans">Available</div>
                </div>
                <div className="p-2 rounded-lg bg-amber-950/30 border border-amber-500/20">
                  <div className="text-amber-400 font-extrabold text-sm">{product.reservedStock}</div>
                  <div className="text-slate-400 text-[10px] uppercase font-sans">Reserved</div>
                </div>
                <div className="p-2 rounded-lg bg-indigo-950/30 border border-indigo-500/20">
                  <div className="text-indigo-400 font-extrabold text-sm">{product.soldStock}</div>
                  <div className="text-slate-400 text-[10px] uppercase font-sans">Sold</div>
                </div>
              </div>
            </div>

            {/* Primary Action Button */}
            <div>
              {activeReservation ? (
                <button
                  onClick={onOpenCheckout}
                  className="w-full py-4 px-6 rounded-2xl font-black text-slate-950 bg-gradient-to-r from-emerald-400 via-teal-300 to-emerald-400 hover:from-emerald-300 hover:to-teal-200 shadow-xl shadow-emerald-500/20 flex items-center justify-center gap-3 transition-all transform hover:-translate-y-0.5 text-base cursor-pointer"
                >
                  <Lock className="w-5 h-5 text-slate-950" />
                  <span>PROCEED TO SECURE CHECKOUT</span>
                  <ArrowRight className="w-5 h-5" />
                </button>
              ) : isOutOfStock ? (
                <button
                  disabled
                  className="w-full py-4 px-6 rounded-2xl font-bold text-slate-400 bg-slate-800/80 border border-white/5 cursor-not-allowed flex items-center justify-center gap-2"
                >
                  <AlertCircle className="w-5 h-5" />
                  <span>ALL UNITS CURRENTLY RESERVED OR SOLD</span>
                </button>
              ) : (
                <button
                  id="claim-deal-btn"
                  onClick={handleReserve}
                  disabled={reserving}
                  className="w-full py-4 px-6 rounded-2xl font-black text-slate-950 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-300 hover:from-amber-300 hover:to-orange-300 shadow-xl shadow-amber-500/25 flex items-center justify-center gap-3 transition-all transform hover:-translate-y-0.5 text-base cursor-pointer"
                >
                  {reserving ? (
                    <>
                      <div className="w-5 h-5 border-3 border-slate-950 border-t-transparent rounded-full animate-spin" />
                      <span>ACQUIRING ATOMIC LOCK...</span>
                    </>
                  ) : (
                    <>
                      <Zap className="w-5 h-5 fill-slate-950" />
                      <span>CLAIM DEAL & RESERVE STOCK (1 UNIT)</span>
                    </>
                  )}
                </button>
              )}

              <p className="text-center text-[11px] text-slate-400 mt-3 flex items-center justify-center gap-1.5">
                <Shield className="w-3.5 h-3.5 text-emerald-400" />
                Protected against duplicate clicks with Client-Generated Idempotency Keys
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
