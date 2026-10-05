'use client';

import React, { useState, useEffect } from 'react';
import {
  X,
  CreditCard,
  QrCode,
  Building,
  Wallet,
  ShieldCheck,
  AlertTriangle,
  Clock,
  Lock,
  ArrowRight,
  CheckCircle2,
  RefreshCw,
} from 'lucide-react';
import { ReservationResponse, CheckoutResponse, PaymentResponse, UserSession } from '@/types';
import { api, generateIdempotencyKey } from '@/lib/api';

interface CheckoutModalProps {
  isOpen: boolean;
  onClose: () => void;
  reservation: ReservationResponse | null;
  userSession: UserSession;
  onPaymentComplete: (payment: PaymentResponse) => void;
  onReservationExpired: () => void;
}

export const CheckoutModal: React.FC<CheckoutModalProps> = ({
  isOpen,
  onClose,
  reservation,
  userSession,
  onPaymentComplete,
  onReservationExpired,
}) => {
  const [checkoutSession, setCheckoutSession] = useState<CheckoutResponse | null>(null);
  const [loadingSession, setLoadingSession] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<string>('MOCK_CARD');
  const [scenario, setScenario] = useState<'SUCCESS' | 'FAILURE' | 'TIMEOUT'>('SUCCESS');
  const [idempotencyKey, setIdempotencyKey] = useState<string>('');
  const [processing, setProcessing] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [paymentResult, setPaymentResult] = useState<PaymentResponse | null>(null);
  const [remainingTime, setRemainingTime] = useState<number>(0);

  // Initialize or fetch checkout session when modal opens
  useEffect(() => {
    if (!isOpen || !reservation) return;

    setIdempotencyKey(generateIdempotencyKey('pay'));
    setPaymentResult(null);
    setErrorMessage(null);

    const initSession = async () => {
      setLoadingSession(true);
      try {
        const session = await api.createCheckoutSession({
          reservationId: reservation.reservationId,
          customerId: userSession.customerId,
        });
        setCheckoutSession(session);
      } catch (err: unknown) {
        if (err instanceof Error) {
          setErrorMessage(err.message);
        }
      } finally {
        setLoadingSession(false);
      }
    };

    initSession();
  }, [isOpen, reservation, userSession.customerId]);

  // Expiry countdown ticker
  useEffect(() => {
    if (!reservation) return;

    const expiresAt = new Date(reservation.expiresAt).getTime();
    const updateTime = () => {
      const diff = Math.max(0, Math.floor((expiresAt - Date.now()) / 1000));
      setRemainingTime(diff);
      if (diff === 0) {
        onReservationExpired();
        onClose();
      }
    };

    updateTime();
    const interval = setInterval(updateTime, 1000);
    return () => clearInterval(interval);
  }, [reservation, onReservationExpired, onClose]);

  const handlePay = async () => {
    if (!checkoutSession) return;
    setProcessing(true);
    setErrorMessage(null);

    try {
      const res = await api.processPayment(checkoutSession.checkoutSessionId, {
        checkoutSessionId: checkoutSession.checkoutSessionId,
        customerId: userSession.customerId,
        idempotencyKey,
        paymentMethod,
        scenario,
      });

      setPaymentResult(res);
      if (res.status === 'SUCCEEDED') {
        onPaymentComplete(res);
      } else if (res.status === 'FAILED') {
        setErrorMessage(`Payment Rejected by Gateway: ${res.failureReason || 'Declined'}`);
      } else if (res.status === 'TIMED_OUT') {
        setErrorMessage('Payment Gateway Timed Out. Circuit Breaker logged failure. Reconciliation pending.');
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMessage(err.message);
      }
    } finally {
      setProcessing(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md overflow-y-auto animate-in fade-in duration-200">
      <div className="relative w-full max-w-xl rounded-3xl glass-panel border border-white/15 bg-slate-900 shadow-2xl p-6 sm:p-8 space-y-6 my-8">
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-6 right-6 p-2 rounded-xl text-slate-400 hover:text-white hover:bg-white/10 transition"
        >
          <X className="w-5 h-5" />
        </button>

        {paymentResult && paymentResult.status === 'SUCCEEDED' ? (
          /* Payment Succeeded Screen */
          <div className="text-center py-6 space-y-5">
            <div className="w-16 h-16 rounded-2xl bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center mx-auto glow-emerald">
              <CheckCircle2 className="w-10 h-10" />
            </div>

            <div>
              <span className="px-3 py-1 rounded-full text-xs font-mono font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                PAYMENT CONFIRMED
              </span>
              <h2 className="text-2xl font-black text-white mt-2">Flash Sale Order Placed!</h2>
              <p className="text-xs text-slate-300 mt-1">
                Your reservation was permanently converted to a confirmed order.
              </p>
            </div>

            <div className="p-4 rounded-2xl bg-slate-950/70 border border-white/10 text-left font-mono text-xs space-y-2">
              <div className="flex justify-between">
                <span className="text-slate-400">Order Number:</span>
                <span className="text-amber-400 font-bold">{paymentResult.orderNumber}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Transaction Ref:</span>
                <span className="text-slate-200">{paymentResult.transactionReference}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Amount Paid:</span>
                <span className="text-emerald-400 font-bold">₹{paymentResult.amount}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-400">Idempotency Key:</span>
                <span className="text-slate-400 truncate max-w-[200px]">
                  {paymentResult.idempotencyKey}
                </span>
              </div>
            </div>

            <button
              onClick={onClose}
              className="w-full py-3.5 rounded-xl font-bold bg-amber-500 hover:bg-amber-400 text-slate-950 shadow-lg transition cursor-pointer"
            >
              View Order Tracking
            </button>
          </div>
        ) : (
          /* Checkout Session Form */
          <>
            {/* Header with Timer */}
            <div className="flex items-center justify-between border-b border-white/10 pb-4">
              <div>
                <h2 className="text-xl font-extrabold text-white flex items-center gap-2">
                  <Lock className="w-5 h-5 text-amber-400" /> Secure Checkout
                </h2>
                <p className="text-xs text-slate-400">Stock locked in Redis cache</p>
              </div>

              {/* Countdown Pill */}
              <div className="flex items-center gap-2 bg-slate-950 px-3 py-1.5 rounded-xl border border-amber-500/30 font-mono text-xs">
                <Clock className="w-4 h-4 text-amber-400 animate-spin" />
                <span className="text-slate-400">Time Left:</span>
                <span className="text-amber-400 font-black">
                  {Math.floor(remainingTime / 60)}:
                  {String(remainingTime % 60).padStart(2, '0')}
                </span>
              </div>
            </div>

            {loadingSession ? (
              <div className="py-12 text-center text-slate-400 space-y-2">
                <RefreshCw className="w-8 h-8 animate-spin mx-auto text-amber-400" />
                <p className="text-xs">Initializing orchestrated checkout session...</p>
              </div>
            ) : checkoutSession ? (
              <div className="space-y-6">
                {/* Order Summary Card */}
                <div className="p-4 rounded-2xl bg-slate-950/60 border border-white/10 space-y-3">
                  <div className="flex justify-between items-center text-sm font-semibold text-white">
                    <span>{checkoutSession.productName}</span>
                    <span className="text-amber-400 font-mono">₹{checkoutSession.totalAmount}</span>
                  </div>
                  <div className="flex justify-between text-xs text-slate-400">
                    <span>Quantity: {checkoutSession.quantity} unit</span>
                    <span>Reservation #{checkoutSession.reservationId}</span>
                  </div>
                  <div className="border-t border-white/5 pt-2 flex justify-between text-xs font-bold text-slate-300">
                    <span>Total Due</span>
                    <span className="text-emerald-400 font-mono text-sm">
                      ₹{checkoutSession.totalAmount}
                    </span>
                  </div>
                </div>

                {/* Payment Method Selector */}
                <div className="space-y-2">
                  <label className="text-xs font-bold uppercase tracking-wider text-slate-400">
                    Select Payment Method
                  </label>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                    {[
                      { id: 'MOCK_CARD', label: 'Credit Card', icon: CreditCard },
                      { id: 'UPI_QR', label: 'UPI / QR', icon: QrCode },
                      { id: 'NETBANKING', label: 'NetBanking', icon: Building },
                      { id: 'WALLET', label: 'Wallet', icon: Wallet },
                    ].map((item) => {
                      const Icon = item.icon;
                      const selected = paymentMethod === item.id;
                      return (
                        <button
                          key={item.id}
                          type="button"
                          onClick={() => setPaymentMethod(item.id)}
                          className={`p-3 rounded-xl border flex flex-col items-center gap-1.5 transition text-xs font-semibold ${
                            selected
                              ? 'bg-amber-500/20 border-amber-500 text-amber-300'
                              : 'bg-white/5 border-white/10 text-slate-400 hover:text-white hover:bg-white/10'
                          }`}
                        >
                          <Icon className="w-5 h-5" />
                          <span>{item.label}</span>
                        </button>
                      );
                    })}
                  </div>
                </div>

                {/* System Design Hackathon Scenario Switcher */}
                <div className="p-4 rounded-2xl bg-indigo-950/20 border border-indigo-500/30 space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-indigo-300 uppercase tracking-wider flex items-center gap-1.5">
                      <ShieldCheck className="w-3.5 h-3.5" /> Gateway Scenario Simulator
                    </span>
                    <span className="text-[10px] text-slate-400 font-mono">Resilience4j Circuit Breaker</span>
                  </div>
                  <p className="text-[11px] text-slate-300">
                    Test how the distributed engine behaves across different real-world gateway responses:
                  </p>
                  <div className="grid grid-cols-3 gap-2">
                    {[
                      { id: 'SUCCESS', label: '🟢 Success', desc: 'Confirm & Outbox' },
                      { id: 'FAILURE', label: '🔴 Card Declined', desc: 'Release stock' },
                      { id: 'TIMEOUT', label: '🟡 Gateway Timeout', desc: 'Circuit Breaker' },
                    ].map((sc) => (
                      <button
                        key={sc.id}
                        type="button"
                        onClick={() => setScenario(sc.id as 'SUCCESS' | 'FAILURE' | 'TIMEOUT')}
                        className={`p-2 rounded-lg border text-left text-xs transition ${
                          scenario === sc.id
                            ? 'bg-indigo-600/30 border-indigo-400 text-white font-bold'
                            : 'bg-slate-900/60 border-white/10 text-slate-400 hover:text-slate-200'
                        }`}
                      >
                        <div>{sc.label}</div>
                        <div className="text-[10px] text-slate-400 mt-0.5">{sc.desc}</div>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Idempotency Key Bar */}
                <div className="flex items-center justify-between bg-slate-950/80 px-3 py-2 rounded-xl border border-white/5 font-mono text-[11px] text-slate-400">
                  <span>Idempotency Key:</span>
                  <span className="text-slate-300 truncate max-w-[200px]">{idempotencyKey}</span>
                  <button
                    onClick={() => setIdempotencyKey(generateIdempotencyKey('pay'))}
                    title="Generate New Key"
                    className="text-amber-400 hover:text-amber-300 text-xs ml-2"
                  >
                    Regen
                  </button>
                </div>

                {/* Error Banner */}
                {errorMessage && (
                  <div className="rounded-xl bg-rose-500/10 border border-rose-500/30 p-3 text-xs text-rose-300 flex items-center gap-2">
                    <AlertTriangle className="w-4 h-4 flex-shrink-0 text-rose-400" />
                    <span>{errorMessage}</span>
                  </div>
                )}

                {/* Pay Button */}
                <button
                  id="authorize-pay-btn"
                  onClick={handlePay}
                  disabled={processing}
                  className="w-full py-4 rounded-2xl font-black text-slate-950 bg-gradient-to-r from-amber-400 via-orange-400 to-amber-300 hover:from-amber-300 hover:to-orange-300 shadow-xl shadow-amber-500/20 flex items-center justify-center gap-2 transition transform hover:-translate-y-0.5 cursor-pointer"
                >
                  {processing ? (
                    <>
                      <RefreshCw className="w-5 h-5 animate-spin" />
                      <span>AUTHORIZING VIA GATEWAY...</span>
                    </>
                  ) : (
                    <>
                      <Lock className="w-4 h-4" />
                      <span>PAY ₹{checkoutSession.totalAmount} & CONFIRM</span>
                    </>
                  )}
                </button>
              </div>
            ) : null}
          </>
        )}
      </div>
    </div>
  );
};
