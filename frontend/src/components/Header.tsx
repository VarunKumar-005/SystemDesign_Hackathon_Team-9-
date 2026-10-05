'use client';

import React from 'react';
import {
  Zap,
  Activity,
  Layers,
  ShoppingBag,
  Gauge,
  Cpu,
  User,
  ShieldCheck,
  AlertTriangle,
  RefreshCw,
} from 'lucide-react';
import { UserSession } from '@/types';

interface HeaderProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  activeReservationCount: number;
  ordersCount: number;
  userSession: UserSession;
  setUserSession: (session: UserSession) => void;
  orderServiceOnline: boolean;
  onRefreshAll: () => void;
  isRefreshing: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  activeTab,
  setActiveTab,
  activeReservationCount,
  ordersCount,
  userSession,
  setUserSession,
  orderServiceOnline,
  onRefreshAll,
  isRefreshing,
}) => {
  const switchUser = (role: 'CUSTOMER' | 'ADMIN') => {
    if (role === 'CUSTOMER') {
      setUserSession({
        customerId: 1,
        email: 'customer@salestorm.io',
        fullName: 'Demo Customer',
        role: 'CUSTOMER',
      });
    } else {
      setUserSession({
        customerId: 2,
        email: 'admin@salestorm.io',
        fullName: 'SysCrafters Admin',
        role: 'ADMIN',
      });
    }
  };

  const navItems = [
    {
      id: 'flash-sale',
      label: 'Flash Sale',
      icon: Zap,
      badge: activeReservationCount > 0 ? `${activeReservationCount} Reserved` : undefined,
      badgeColor: 'bg-amber-500 text-black',
    },
    {
      id: 'orders',
      label: 'Live Orders',
      icon: ShoppingBag,
      badge: ordersCount > 0 ? `${ordersCount}` : undefined,
      badgeColor: 'bg-indigo-500 text-white',
    },
    {
      id: 'monitor',
      label: 'Architecture & Monitor',
      icon: Gauge,
      badge: !orderServiceOnline ? 'DEGRADED' : undefined,
      badgeColor: 'bg-rose-500 text-white animate-pulse',
    },
    {
      id: 'load-test',
      label: 'Load Test Lab',
      icon: Cpu,
      badge: 'Zero-Oversell',
      badgeColor: 'bg-emerald-500 text-black',
    },
  ];

  return (
    <header className="sticky top-0 z-50 w-full glass-panel border-b border-white/10 bg-slate-950/80 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <div className="flex items-center gap-3 cursor-pointer" onClick={() => setActiveTab('flash-sale')}>
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-amber-500 via-orange-500 to-amber-300 flex items-center justify-center shadow-lg shadow-amber-500/20 text-slate-950 font-black animate-pulse-subtle">
              <Zap className="w-6 h-6 fill-slate-950" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-xl tracking-tight text-white">
                  SALE<span className="text-amber-400">STORM</span>
                </span>
                <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-full bg-amber-500/20 text-amber-300 border border-amber-500/30">
                  Prototype
                </span>
              </div>
              <p className="text-xs text-slate-400 font-medium">SysCrafters • High-Scale Engine</p>
            </div>
          </div>

          {/* Navigation Tabs */}
          <nav className="hidden md:flex items-center space-x-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  id={`nav-tab-${item.id}`}
                  onClick={() => setActiveTab(item.id)}
                  className={`flex items-center gap-2 px-3.5 py-2 rounded-lg text-sm font-semibold transition-all duration-200 relative ${
                    isActive
                      ? 'bg-white/10 text-white shadow-inner border border-white/15'
                      : 'text-slate-400 hover:text-white hover:bg-white/5'
                  }`}
                >
                  <Icon className={`w-4 h-4 ${isActive ? 'text-amber-400' : 'text-slate-400'}`} />
                  <span>{item.label}</span>
                  {item.badge && (
                    <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded-full ${item.badgeColor}`}>
                      {item.badge}
                    </span>
                  )}
                  {isActive && (
                    <span className="absolute bottom-0 left-2 right-2 h-0.5 bg-gradient-to-r from-amber-400 to-orange-400 rounded-full" />
                  )}
                </button>
              );
            })}
          </nav>

          {/* Right Status & Controls */}
          <div className="flex items-center gap-3">
            {/* System Status Indicators */}
            <div className="hidden lg:flex items-center gap-2 bg-slate-900/90 px-3 py-1.5 rounded-full border border-white/5 text-xs">
              <span className="flex h-2 w-2 relative">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
              </span>
              <span className="text-slate-300 font-medium">API: 8080</span>
              <span className="text-slate-600">|</span>
              <span className="text-slate-300 font-medium">Redis & DB</span>
              <span className="text-slate-600">|</span>
              {orderServiceOnline ? (
                <span className="flex items-center gap-1 text-emerald-400 font-semibold">
                  <ShieldCheck className="w-3.5 h-3.5" /> Order Svc ON
                </span>
              ) : (
                <span className="flex items-center gap-1 text-rose-400 font-semibold animate-pulse">
                  <AlertTriangle className="w-3.5 h-3.5" /> Order Svc OFF
                </span>
              )}
            </div>

            {/* Refresh Button */}
            <button
              onClick={onRefreshAll}
              title="Refresh Data from Backend"
              className="p-2 rounded-lg bg-white/5 hover:bg-white/10 text-slate-300 hover:text-white border border-white/10 transition"
            >
              <RefreshCw className={`w-4 h-4 ${isRefreshing ? 'animate-spin text-amber-400' : ''}`} />
            </button>

            {/* User Session Role Switcher */}
            <div className="flex items-center bg-slate-900/90 rounded-lg p-1 border border-white/10 text-xs font-semibold">
              <button
                onClick={() => switchUser('CUSTOMER')}
                className={`px-2.5 py-1 rounded transition ${
                  userSession.role === 'CUSTOMER'
                    ? 'bg-amber-500 text-slate-950 shadow'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                Customer
              </button>
              <button
                onClick={() => switchUser('ADMIN')}
                className={`px-2.5 py-1 rounded transition ${
                  userSession.role === 'ADMIN'
                    ? 'bg-indigo-600 text-white shadow'
                    : 'text-slate-400 hover:text-white'
                }`}
              >
                Admin
              </button>
            </div>
          </div>
        </div>

        {/* Mobile Navigation */}
        <div className="flex md:hidden overflow-x-auto py-2 space-x-2 border-t border-white/5">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap ${
                  isActive ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30' : 'text-slate-400'
                }`}
              >
                <Icon className="w-3.5 h-3.5" />
                <span>{item.label}</span>
                {item.badge && (
                  <span className={`text-[9px] px-1 rounded-full ${item.badgeColor}`}>{item.badge}</span>
                )}
              </button>
            );
          })}
        </div>
      </div>
    </header>
  );
};
