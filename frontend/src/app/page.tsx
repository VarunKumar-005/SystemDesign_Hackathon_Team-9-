'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { Header } from '@/components/Header';
import { FlashSaleView } from '@/components/FlashSaleView';
import { CheckoutModal } from '@/components/CheckoutModal';
import { OrdersView } from '@/components/OrdersView';
import { MonitoringView } from '@/components/MonitoringView';
import { LoadTestStudio } from '@/components/LoadTestStudio';
import {
  ProductDetailDTO,
  ReservationResponse,
  OrderResponse,
  MonitoringMetricsDTO,
  UserSession,
  PaymentResponse,
} from '@/types';
import { api } from '@/lib/api';

export default function Home() {
  const [activeTab, setActiveTab] = useState<string>('flash-sale');
  const [product, setProduct] = useState<ProductDetailDTO | null>(null);
  const [activeReservation, setActiveReservation] = useState<ReservationResponse | null>(null);
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [metrics, setMetrics] = useState<MonitoringMetricsDTO | null>(null);
  const [orderServiceOnline, setOrderServiceOnline] = useState<boolean>(true);
  const [isCheckoutOpen, setIsCheckoutOpen] = useState<boolean>(false);
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);

  // Active User Profile: Customer 1 default, toggleable to Admin 2
  const [userSession, setUserSession] = useState<UserSession>({
    customerId: 1,
    email: 'customer@salestorm.io',
    fullName: 'Demo Customer',
    role: 'CUSTOMER',
  });

  const loadData = useCallback(async () => {
    setIsRefreshing(true);
    try {
      // 1. Fetch Flash Sale Product #1
      const prod = await api.getProductById(1);
      setProduct(prod);

      // 2. Fetch System Metrics
      const m = await api.getMetrics();
      setMetrics(m);
      if (m?.orderServiceAvailable !== undefined) {
        setOrderServiceOnline(m.orderServiceAvailable);
      }

      // 3. Fetch Orders for active user
      const userOrders = await api.getOrders(
        userSession.role === 'ADMIN' ? undefined : userSession.customerId
      );
      setOrders(userOrders);
    } catch (err) {
      console.error('Failed to load initial data from backend:', err);
    } finally {
      setIsRefreshing(false);
    }
  }, [userSession.role, userSession.customerId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handlePaymentComplete = (payment: PaymentResponse) => {
    setActiveReservation(null);
    loadData();
    // Switch to orders tab to see the live order
    setActiveTab('orders');
  };

  const handleReservationExpired = () => {
    setActiveReservation(null);
    loadData();
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#0b0f17] text-slate-100 selection:bg-amber-500 selection:text-slate-950 font-sans">
      {/* Top Header */}
      <Header
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        activeReservationCount={activeReservation ? 1 : 0}
        ordersCount={orders.length}
        userSession={userSession}
        setUserSession={setUserSession}
        orderServiceOnline={orderServiceOnline}
        onRefreshAll={loadData}
        isRefreshing={isRefreshing}
      />

      {/* Main Content Area */}
      <main className="flex-1">
        {activeTab === 'flash-sale' && (
          <FlashSaleView
            product={product}
            activeReservation={activeReservation}
            setActiveReservation={setActiveReservation}
            onOpenCheckout={() => setIsCheckoutOpen(true)}
            userSession={userSession}
            onRefresh={loadData}
          />
        )}

        {activeTab === 'orders' && (
          <OrdersView userSession={userSession} onRefresh={loadData} />
        )}

        {activeTab === 'monitor' && (
          <MonitoringView
            metrics={metrics}
            onRefresh={loadData}
            orderServiceOnline={orderServiceOnline}
            setOrderServiceOnline={setOrderServiceOnline}
          />
        )}

        {activeTab === 'load-test' && <LoadTestStudio onRefresh={loadData} />}
      </main>

      {/* Checkout Modal */}
      <CheckoutModal
        isOpen={isCheckoutOpen}
        onClose={() => setIsCheckoutOpen(false)}
        reservation={activeReservation}
        userSession={userSession}
        onPaymentComplete={handlePaymentComplete}
        onReservationExpired={handleReservationExpired}
      />

      {/* Footer */}
      <footer className="glass-panel border-t border-white/5 py-8 mt-16 bg-slate-950/60">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row items-center justify-between gap-4 text-xs text-slate-400">
          <div>
            <span className="font-extrabold text-white tracking-tight">
              SALESTORM <span className="text-amber-400 font-mono text-[11px]">v0.0.1</span>
            </span>{' '}
            • High-Scale Flash Sale Prototype
            <div className="text-[11px] text-slate-400 mt-0.5">
              Built for System Design Hackathon • Team 9 (SysCrafters)
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-4 text-[11px] font-mono">
            <span className="px-2 py-0.5 rounded bg-white/5 border border-white/10">
              Spring Boot 3.4 (Java 21)
            </span>
            <span className="px-2 py-0.5 rounded bg-white/5 border border-white/10">
              Next.js 16 (Turbopack)
            </span>
            <span className="px-2 py-0.5 rounded bg-white/5 border border-white/10">
              PostgreSQL 18
            </span>
            <span className="px-2 py-0.5 rounded bg-white/5 border border-white/10">
              Redis Lock (TTL 300s)
            </span>
            <span className="px-2 py-0.5 rounded bg-white/5 border border-white/10">
              Transactional Outbox
            </span>
          </div>
        </div>
      </footer>
    </div>
  );
}
