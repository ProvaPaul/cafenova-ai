import { useState } from 'react'
import { useCart } from '../../contexts/CartContext'
import { useNavigate } from 'react-router-dom'
import api from '../../api/client'
import toast from 'react-hot-toast'

export default function CheckoutPage() {
  const { cart, total, clearCart } = useCart()
  const navigate = useNavigate()
  const [form, setForm] = useState({ orderType: 'TAKEAWAY', notes: '', couponCode: '' })
  const [loading, setLoading] = useState(false)

  const f = k => e => setForm(p => ({...p, [k]: e.target.value}))

  const submit = async (e) => {
    e.preventDefault()
    if (cart.length === 0) { toast.error('Cart is empty'); return }
    setLoading(true)
    try {
      const res = await api.post('/orders/checkout', form)
      const order = res.data.data
      toast.success(`Order ${order.orderNumber} placed!`)
      navigate(`/orders/${order.id}`)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Checkout failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Checkout</h1>
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
        <form onSubmit={submit} className="lg:col-span-3 space-y-5">
          {/* Order type */}
          <div className="card p-5">
            <h2 className="font-semibold text-gray-900 mb-3">Order Type</h2>
            <div className="grid grid-cols-2 gap-3">
              {['TAKEAWAY', 'DELIVERY'].map(type => (
                <label key={type} className={`cursor-pointer border-2 rounded-lg p-3 text-center transition-colors ${form.orderType === type ? 'border-cafe-500 bg-cafe-50 text-cafe-700' : 'border-gray-200 text-gray-600'}`}>
                  <input type="radio" name="orderType" value={type} className="sr-only"
                    checked={form.orderType === type} onChange={f('orderType')} />
                  <div className="text-2xl mb-1">{type === 'TAKEAWAY' ? '🥡' : '🚚'}</div>
                  <div className="font-medium text-sm">{type === 'TAKEAWAY' ? 'Takeaway' : 'Delivery'}</div>
                </label>
              ))}
            </div>
          </div>

          {/* Coupon */}
          <div className="card p-5">
            <h2 className="font-semibold text-gray-900 mb-3">Coupon Code</h2>
            <div className="flex gap-2">
              <input className="input-field flex-1" placeholder="Enter coupon code (optional)"
                value={form.couponCode} onChange={f('couponCode')} />
            </div>
            <p className="text-xs text-gray-400 mt-1">Try WELCOME10 for 10% off your first order</p>
          </div>

          {/* Notes */}
          <div className="card p-5">
            <h2 className="font-semibold text-gray-900 mb-3">Special Instructions</h2>
            <textarea className="input-field resize-none" rows={3} placeholder="Allergies, preferences, etc."
              value={form.notes} onChange={f('notes')} />
          </div>

          <button type="submit" disabled={loading || cart.length === 0} className="btn-primary w-full py-3 text-base">
            {loading ? 'Placing Order…' : 'Place Order'}
          </button>
        </form>

        {/* Summary */}
        <div className="lg:col-span-2">
          <div className="card p-5 sticky top-20">
            <h2 className="font-bold text-gray-900 mb-4">Order Items</h2>
            <div className="space-y-2 mb-4">
              {cart.map(item => (
                <div key={item.id} className="flex justify-between text-sm">
                  <span className="text-gray-700">{item.menuItem?.name} × {item.quantity}</span>
                  <span className="font-medium">₱{(Number(item.menuItem?.price) * item.quantity).toFixed(2)}</span>
                </div>
              ))}
            </div>
            <hr className="border-gray-100 mb-3" />
            <div className="space-y-1 text-sm text-gray-600">
              <div className="flex justify-between"><span>Subtotal</span><span>₱{total.toFixed(2)}</span></div>
              <div className="flex justify-between"><span>Tax (12%)</span><span>₱{(total*0.12).toFixed(2)}</span></div>
            </div>
            <hr className="border-gray-100 my-3" />
            <div className="flex justify-between font-bold text-gray-900">
              <span>Total</span>
              <span className="text-cafe-700">₱{(total*1.12).toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
