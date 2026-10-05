'use client';

import React, { useState, useEffect } from 'react';
import {
  Package,
  Clock,
  CheckCircle,
  Truck,
  Box,
  XCircle,
  ChevronRight,
  RefreshCw,
  ShoppingBag,
  ArrowRight,
  ShieldAlert,
} from 'lucide-react';
import { OrderResponse, UserSession } from '@/types';
import { api } from '@/lib/api';

interface OrdersViewProps {
  userSession: UserSession;
  onRefresh: () => void;
}

export const OrdersView: React.FC<OrdersViewProps> = ({ userSession, onRefresh }) => {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchOrders = async () => {
    setLoading(true);
    setErrorMessage(null);
    try {
      // If admin, show all orders; if customer, show customer orders
      const data = await api.getOrders(userSession.role === 'ADMIN' ? undefined : userSession.customerId);
      setOrders(data);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMessage(err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [userSession.role, userSession.customerId]);

  const handleAdvanceStatus = async (order: OrderResponse) => {
    const statusPipeline = ['CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];
    const currentIndex = statusPipeline.indexOf(order.status);
    if (currentIndex === -1 || currentIndex >= statusPipeline.length - 1) return;

    const nextStatus = statusPipeline[currentIndex + 1];
    setUpdatingId(order.orderId);

    try {
      await api.updateOrderStatus(order.orderId, nextStatus, `Advanced to ${nextStatus} via Hackathon Control`);
      await fetchOrders();
      onRefresh();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMessage(err.message);
      }
    } finally {
      setUpdatingId(null);
    }
  };

  const handleCancelOrder = async (orderId: number) => {
    if (!confirm('Are you sure you want to cancel this order?')) return;
    setUpdatingId(orderId);
    try {
      await api.cancelOrder(orderId, 'Cancelled by user');
      await fetchOrders();
      onRefresh();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErrorMessage(err.message);
      }
    } finally {
      setUpdatingId(null);
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'CONFIRMED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">CONFIRMED</span>;
      case 'PROCESSING':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">PROCESSING</span>;
      case 'SHIPPED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">SHIPPED</span>;
      case 'DELIVERED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-400 text-slate-950 font-black">DELIVERED</span>;
      case 'CANCELLED':
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-rose-500/20 text-rose-400 border border-rose-500/30">CANCELLED</span>;
      default:
        return <span className="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30">{status}</span>;
    }
  };

  const stages = [
    { id: 'CONFIRMED', label: 'Confirmed', icon: CheckCircle },
    { id: 'PROCESSING', label: 'Processing', icon: Box },
    { id: 'SHIPPED', label: 'Shipped', icon: Truck },
    { id: 'DELIVERED', label: 'Delivered', icon: Package },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <ShoppingBag className="w-6 h-6 text-amber-400" />
            <span>Order Lifecycle & Tracking</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Viewing orders for {userSession.role === 'ADMIN' ? 'All Customers (Admin Mode)' : userSession.fullName}
          </p>
        </div>

        <button
          onClick={fetchOrders}
          disabled={loading}
          className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 text-xs font-semibold text-slate-300 hover:text-white transition"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh Orders</span>
        </button>
      </div>

      {errorMessage && (
        <div className="rounded-xl bg-rose-500/10 border border-rose-500/30 p-4 text-xs text-rose-300">
          {errorMessage}
        </div>
      )}

      {loading && orders.length === 0 ? (
        <div className="py-16 text-center text-slate-400">
          <RefreshCw className="w-8 h-8 animate-spin mx-auto text-amber-400 mb-2" />
          <p className="text-xs font-medium">Fetching orders from PostgreSQL...</p>
        </div>
      ) : orders.length === 0 ? (
        <div className="glass-panel rounded-3xl p-12 text-center border border-white/10 space-y-3">
          <Package className="w-12 h-12 text-slate-600 mx-auto" />
          <h3 className="text-base font-bold text-slate-300">No Orders Found Yet</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            Reserve a unit in the Flash Sale tab and complete payment to watch the distributed order pipeline in action!
          </p>
        </div>
      ) : (
        <div className="space-y-6">
          {orders.map((order) => {
            const isCancelled = order.status === 'CANCELLED';
            const currentStageIndex = stages.findIndex((s) => s.id === order.status);

            return (
              <div
                key={order.orderId}
                className="glass-panel rounded-3xl p-6 border border-white/10 space-y-6 glass-card-hover"
              >
                {/* Order Top Bar */}
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-b border-white/10 pb-4">
                  <div className="flex items-center gap-3">
                    <div className="p-3 rounded-2xl bg-amber-500/10 border border-amber-500/20 text-amber-400">
                      <Package className="w-6 h-6" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-mono font-bold text-white text-base">
                          {order.orderNumber}
                        </span>
                        {getStatusBadge(order.status)}
                      </div>
                      <p className="text-xs text-slate-400 mt-0.5">
                        Placed on {new Date(order.createdAt).toLocaleString()} • Txn:{' '}
                        <span className="font-mono text-slate-300">
                          {order.paymentTransactionRef || 'N/A'}
                        </span>
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 w-full sm:w-auto justify-between sm:justify-end">
                    <div className="text-right">
                      <div className="text-xs text-slate-400">Total Amount</div>
                      <div className="text-lg font-black text-amber-400 font-mono">
                        ₹{order.totalAmount}
                      </div>
                    </div>

                    {/* Admin Status Advancer */}
                    {!isCancelled && order.status !== 'DELIVERED' && (
                      <button
                        onClick={() => handleAdvanceStatus(order)}
                        disabled={updatingId === order.orderId}
                        title="Simulate fulfillment pipeline advancement"
                        className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition shadow"
                      >
                        {updatingId === order.orderId ? (
                          <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                        ) : (
                          <>
                            <span>Advance Status</span>
                            <ArrowRight className="w-3.5 h-3.5" />
                          </>
                        )}
                      </button>
                    )}

                    {!isCancelled && order.status === 'CONFIRMED' && (
                      <button
                        onClick={() => handleCancelOrder(order.orderId)}
                        disabled={updatingId === order.orderId}
                        className="px-2.5 py-1.5 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 text-rose-300 border border-rose-500/20 text-xs font-semibold transition"
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </div>

                {/* Status Timeline */}
                {!isCancelled ? (
                  <div className="py-2">
                    <div className="relative flex items-center justify-between">
                      {/* Connecting line */}
                      <div className="absolute top-1/2 left-0 right-0 h-0.5 bg-slate-800 -translate-y-1/2 z-0" />
                      <div
                        className="absolute top-1/2 left-0 h-0.5 bg-gradient-to-r from-emerald-500 to-indigo-500 -translate-y-1/2 z-0 transition-all duration-500"
                        style={{
                          width: `${(Math.max(0, currentStageIndex) / (stages.length - 1)) * 100}%`,
                        }}
                      />

                      {stages.map((stage, idx) => {
                        const Icon = stage.icon;
                        const isCompleted = idx <= currentStageIndex;
                        const isCurrent = idx === currentStageIndex;

                        return (
                          <div key={stage.id} className="relative z-10 flex flex-col items-center">
                            <div
                              className={`w-9 h-9 rounded-full flex items-center justify-center transition-all ${
                                isCurrent
                                  ? 'bg-amber-400 text-slate-950 ring-4 ring-amber-400/20 shadow-lg scale-110'
                                  : isCompleted
                                  ? 'bg-emerald-500 text-slate-950'
                                  : 'bg-slate-800 text-slate-500 border border-white/5'
                              }`}
                            >
                              <Icon className="w-4 h-4" />
                            </div>
                            <span
                              className={`text-[11px] font-bold mt-2 ${
                                isCurrent
                                  ? 'text-amber-400'
                                  : isCompleted
                                  ? 'text-slate-200'
                                  : 'text-slate-500'
                              }`}
                            >
                              {stage.label}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                ) : (
                  <div className="p-3 rounded-2xl bg-rose-950/20 border border-rose-500/30 text-rose-300 text-xs flex items-center gap-2">
                    <XCircle className="w-4 h-4 text-rose-400" />
                    <span>Order Cancelled: {order.cancelReason || 'Customer requested'}</span>
                  </div>
                )}

                {/* Items & Status History Accordion */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs font-mono">
                  {/* Items */}
                  <div className="p-3.5 rounded-2xl bg-slate-950/50 border border-white/5 space-y-2">
                    <span className="text-[10px] uppercase font-sans font-bold text-slate-400">
                      Order Items
                    </span>
                    {order.items?.map((item) => (
                      <div key={item.itemId} className="flex justify-between items-center text-slate-300">
                        <span>
                          {item.productName} × {item.quantity}
                        </span>
                        <span className="font-bold text-amber-400">₹{item.totalPrice}</span>
                      </div>
                    ))}
                  </div>

                  {/* Status Audit Trail */}
                  <div className="p-3.5 rounded-2xl bg-slate-950/50 border border-white/5 space-y-2">
                    <span className="text-[10px] uppercase font-sans font-bold text-slate-400">
                      Audit Trail (DB State Machine)
                    </span>
                    <div className="space-y-1 max-h-24 overflow-y-auto">
                      {order.history?.map((h) => (
                        <div key={h.historyId} className="flex items-center justify-between text-[11px] text-slate-400">
                          <span>
                            <span className="text-slate-500">{h.previousStatus}</span> →{' '}
                            <span className="text-emerald-400 font-semibold">{h.newStatus}</span>
                          </span>
                          <span className="text-[10px] text-slate-600">
                            {new Date(h.createdAt).toLocaleTimeString()}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
