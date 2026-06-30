import { useCart } from '../../contexts/CartContext'
import { Link, useNavigate } from 'react-router-dom'

export default function CartPage() {
  const { cart, total, updateQuantity, removeItem, clearCart } = useCart()
  const navigate = useNavigate()

  if (cart.length === 0) return (
    <div className="max-w-2xl mx-auto px-4 py-20 text-center">
      <div className="text-6xl mb-4">🛒</div>
      <h2 className="text-xl font-bold text-gray-900 mb-2">Your cart is empty</h2>
      <p className="text-gray-500 mb-6">Add some delicious items from our menu!</p>
      <Link to="/menu" className="btn-primary px-8 py-3">Browse Menu</Link>
    </div>
  )

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Your Cart</h1>
        <button onClick={clearCart} className="text-sm text-red-500 hover:underline">Clear all</button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-3">
          {cart.map(item => (
            <div key={item.id} className="card p-4 flex items-center gap-4">
              <div className="w-14 h-14 bg-cafe-100 rounded-lg flex items-center justify-center text-2xl flex-shrink-0">☕</div>
              <div className="flex-1 min-w-0">
                <p className="font-semibold text-gray-900 truncate">{item.menuItem?.name}</p>
                <p className="text-cafe-600 font-medium">₱{Number(item.menuItem?.price).toFixed(2)}</p>
              </div>
              <div className="flex items-center border border-gray-200 rounded-lg overflow-hidden">
                <button onClick={() => updateQuantity(item.id, item.quantity - 1)}
                  className="px-3 py-1.5 hover:bg-gray-50 text-lg leading-none">−</button>
                <span className="px-3 py-1.5 font-semibold">{item.quantity}</span>
                <button onClick={() => updateQuantity(item.id, item.quantity + 1)}
                  className="px-3 py-1.5 hover:bg-gray-50 text-lg leading-none">+</button>
              </div>
              <p className="font-bold text-gray-900 w-20 text-right">
                ₱{(Number(item.menuItem?.price) * item.quantity).toFixed(2)}
              </p>
              <button onClick={() => removeItem(item.id)} className="text-red-400 hover:text-red-600 p-1">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div className="card p-5 h-fit sticky top-20">
          <h2 className="font-bold text-lg text-gray-900 mb-4">Order Summary</h2>
          <div className="space-y-2 text-sm text-gray-600 mb-4">
            <div className="flex justify-between"><span>Subtotal</span><span>₱{total.toFixed(2)}</span></div>
            <div className="flex justify-between"><span>Tax (12%)</span><span>₱{(total * 0.12).toFixed(2)}</span></div>
          </div>
          <hr className="border-gray-100 mb-3" />
          <div className="flex justify-between font-bold text-gray-900 text-base mb-5">
            <span>Total</span>
            <span className="text-cafe-700">₱{(total * 1.12).toFixed(2)}</span>
          </div>
          <button onClick={() => navigate('/checkout')} className="btn-primary w-full py-3">
            Proceed to Checkout
          </button>
          <Link to="/menu" className="block text-center text-sm text-cafe-600 hover:underline mt-3">
            Continue shopping
          </Link>
        </div>
      </div>
    </div>
  )
}
