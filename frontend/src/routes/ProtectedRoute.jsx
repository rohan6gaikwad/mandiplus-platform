import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50">
        <div className="h-8 w-8 rounded-full border-4 border-agrogreen-200 border-t-agrogreen-600 animate-spin"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // TC-PROFILE-006: Intercept and prevent dashboard bypass if profile name is missing
  if (user.isProfileComplete === false) {
    return <Navigate to="/setup-profile" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role) && user.role !== 'ADMIN') {
    // If authenticated but unauthorized, redirect to standard home
    return <Navigate to="/" replace />;
  }

  return children;
}
