import { Routes, Route } from 'react-router-dom'
import Navbar from './components/layout/Navbar'
import Footer from './components/layout/Footer'
import ProtectedRoute from './components/ProtectedRoute'
import Home from './pages/Home'
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import ForgotPassword from './pages/auth/ForgotPassword'
import MenuPage from './pages/menu/MenuPage'
import ProductDetail from './pages/menu/ProductDetail'
import CartPage from './pages/cart/CartPage'
import CheckoutPage from './pages/checkout/CheckoutPage'
import OrderHistory from './pages/orders/OrderHistory'
import OrderTracking from './pages/orders/OrderTracking'
import ReservationPage from './pages/reservations/ReservationPage'
import MyReservations from './pages/reservations/MyReservations'
import ProfilePage from './pages/profile/ProfilePage'
import LoyaltyPage from './pages/loyalty/LoyaltyPage'
import FeedbackPage from './pages/feedback/FeedbackPage'

export default function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-1">
        <Routes>
          <Route path="/"                   element={<Home />} />
          <Route path="/menu"               element={<MenuPage />} />
          <Route path="/menu/:id"           element={<ProductDetail />} />
          <Route path="/login"              element={<Login />} />
          <Route path="/register"           element={<Register />} />
          <Route path="/forgot-password"    element={<ForgotPassword />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/cart"             element={<CartPage />} />
            <Route path="/checkout"         element={<CheckoutPage />} />
            <Route path="/orders"           element={<OrderHistory />} />
            <Route path="/orders/:id"       element={<OrderTracking />} />
            <Route path="/reservations"     element={<ReservationPage />} />
            <Route path="/reservations/my"  element={<MyReservations />} />
            <Route path="/profile"          element={<ProfilePage />} />
            <Route path="/loyalty"          element={<LoyaltyPage />} />
            <Route path="/feedback"         element={<FeedbackPage />} />
          </Route>
        </Routes>
      </main>
      <Footer />
    </div>
  )
}
