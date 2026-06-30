import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/client'

const STATUS_COLOR = {
  PENDING:     'bg-yellow-100 text-yellow-700',
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  READY:       'bg-green-100 text-green-700',
  SERVED:      'bg-green-100 text-green-700',
  COMPLETED:   'bg-gray-100 text-gray-600',
  CANCELLED:   'bg-red-100 text-red-600',
}

export default function OrderHistory() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/orders').then(r => { setOrders(r.data.data || []); setLoading(false) })
  }, [])

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">My Orders</h1>
      {loading ? (
        <div className="space-y-3">{[...Array(3)].map((_,i)=><div key={i} className="card h-24 animate-pulse bg-gray-100"/>)}</div>
      ) : orders.length === 0 ? (
        <div className="text-center py-20 text-gray-400">
          <div className="text-5xl mb-4">📋</div>
          <p className="font-medium">No orders yet</p>
          <Link to="/menu" className="btn-primary inline-block mt-4 px-6">Browse Menu</Link>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map(order => (
            <Link key={order.id} to={`/orders/${order.id}`} className="card p-5 flex items-center gap-4 hover:shadow-md transition-shadow block">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-3 mb-1">
                  <span className="font-bold text-gray-900">{order.orderNumber}</span>
                  <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLOR[order.status] || 'bg-gray-100 text-gray-600'}`}>
                    {order.status}
                  </span>
                </div>
                <p className="text-sm text-gray-500">
                  {order.items?.length || 0} item(s) · {order.orderType} · {new Date(order.createdAt).toLocaleDateString()}
                </p>
              </div>
              <div className="text-right">
                <p className="font-bold text-cafe-700 text-lg">₱{Number(order.total).toFixed(2)}</p>
                <p className="text-xs text-cafe-500 hover:underline">View details →</p>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
