import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import api from '../../api/client'

const STEPS = ['PENDING', 'IN_PROGRESS', 'READY', 'COMPLETED']
const STEP_LABEL = { PENDING: 'Order Received', IN_PROGRESS: 'Preparing', READY: 'Ready for Pickup', COMPLETED: 'Completed' }
const STEP_ICON  = { PENDING: '📋', IN_PROGRESS: '👨‍🍳', READY: '✅', COMPLETED: '⭐' }

export default function OrderTracking() {
  const { id } = useParams()
  const [order, setOrder] = useState(null)

  useEffect(() => {
    api.get(`/orders/${id}`).then(r => setOrder(r.data.data))
    const timer = setInterval(() => api.get(`/orders/${id}`).then(r => setOrder(r.data.data)), 15000)
    return () => clearInterval(timer)
  }, [id])

  if (!order) return <div className="max-w-2xl mx-auto px-4 py-20 animate-pulse"><div className="h-64 bg-gray-100 rounded-xl"/></div>

  const currentStep = STEPS.indexOf(order.status)
  const isCancelled = order.status === 'CANCELLED'

  return (
    <div className="max-w-2xl mx-auto px-4 py-10">
      <Link to="/orders" className="text-cafe-600 hover:underline text-sm mb-6 block">← Back to Orders</Link>

      <div className="card p-6 mb-6">
        <div className="flex items-center justify-between mb-1">
          <h1 className="text-xl font-bold text-gray-900">{order.orderNumber}</h1>
          <span className={`text-sm px-3 py-1 rounded-full font-medium ${isCancelled ? 'bg-red-100 text-red-600' : 'bg-cafe-100 text-cafe-700'}`}>
            {order.status}
          </span>
        </div>
        <p className="text-sm text-gray-500">{order.orderType} · {new Date(order.createdAt).toLocaleString()}</p>
      </div>

      {/* Progress */}
      {!isCancelled && (
        <div className="card p-6 mb-6">
          <h2 className="font-semibold text-gray-900 mb-6">Order Progress</h2>
          <div className="flex items-center">
            {STEPS.map((step, idx) => (
              <div key={step} className="flex items-center flex-1">
                <div className="flex flex-col items-center">
                  <div className={`w-10 h-10 rounded-full flex items-center justify-center text-lg transition-colors ${idx <= currentStep ? 'bg-cafe-600 text-white' : 'bg-gray-100 text-gray-400'}`}>
                    {STEP_ICON[step]}
                  </div>
                  <span className="text-xs mt-1 text-center text-gray-600 max-w-16">{STEP_LABEL[step]}</span>
                </div>
                {idx < STEPS.length - 1 && (
                  <div className={`flex-1 h-0.5 mx-2 transition-colors ${idx < currentStep ? 'bg-cafe-600' : 'bg-gray-200'}`} />
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Items */}
      <div className="card p-5 mb-4">
        <h2 className="font-semibold text-gray-900 mb-4">Items</h2>
        <div className="space-y-2">
          {order.items?.map(item => (
            <div key={item.id} className="flex justify-between text-sm">
              <span className="text-gray-700">{item.menuItemName} × {item.quantity}</span>
              <span className="font-medium">₱{Number(item.subtotal).toFixed(2)}</span>
            </div>
          ))}
        </div>
        <hr className="border-gray-100 my-3" />
        <div className="space-y-1 text-sm text-gray-600">
          <div className="flex justify-between"><span>Subtotal</span><span>₱{Number(order.subtotal).toFixed(2)}</span></div>
          {Number(order.discount) > 0 && <div className="flex justify-between text-green-600"><span>Discount</span><span>−₱{Number(order.discount).toFixed(2)}</span></div>}
          <div className="flex justify-between"><span>Tax</span><span>₱{Number(order.tax).toFixed(2)}</span></div>
        </div>
        <hr className="border-gray-100 my-3" />
        <div className="flex justify-between font-bold text-gray-900">
          <span>Total</span><span className="text-cafe-700">₱{Number(order.total).toFixed(2)}</span>
        </div>
      </div>

      {order.status === 'COMPLETED' && (
        <Link to="/feedback" state={{ orderId: order.id }} className="btn-secondary w-full text-center block py-3">
          Leave a Review ★
        </Link>
      )}
    </div>
  )
}
