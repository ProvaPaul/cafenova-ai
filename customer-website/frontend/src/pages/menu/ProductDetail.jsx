import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../../api/client'
import { useCart } from '../../contexts/CartContext'
import { useAuth } from '../../contexts/AuthContext'
import { useRecommendations } from '../../hooks/useRecommendations'
import RecommendationCard from '../../components/RecommendationCard'
import toast from 'react-hot-toast'

export default function ProductDetail() {
  const { id } = useParams()
  const [item, setItem]       = useState(null)
  const [reviews, setReviews] = useState([])
  const [qty, setQty]         = useState(1)
  const { addItem }           = useCart()
  const { user }              = useAuth()
  const navigate              = useNavigate()

  const { items: similar } = useRecommendations('similar', {
    productId: Number(id),
    name:      item?.name,
    category:  item?.categoryName,
    limit:     4,
  })

  useEffect(() => {
    api.get(`/menu/${id}`).then(r => setItem(r.data.data))
    api.get(`/feedback/public/item/${id}`).then(r => setReviews(r.data.data || []))
  }, [id])

  const handleAdd = async () => {
    if (!user) { navigate('/login'); return }
    try {
      await addItem(item.id, qty)
      toast.success(`${item.name} × ${qty} added to cart`)
    } catch { toast.error('Could not add item') }
  }

  if (!item) return <div className="max-w-2xl mx-auto px-4 py-20 animate-pulse"><div className="h-64 bg-gray-100 rounded-xl" /></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <button onClick={() => navigate(-1)} className="text-cafe-600 hover:underline text-sm mb-6 flex items-center gap-1">
        ← Back to Menu
      </button>
      <div className="card overflow-hidden md:flex">
        <div className="md:w-72 h-64 md:h-auto bg-gradient-to-br from-cafe-100 to-cafe-200 flex items-center justify-center text-8xl">
          ☕
        </div>
        <div className="p-6 flex-1">
          <span className="text-xs bg-cafe-50 text-cafe-600 px-2 py-1 rounded-full">{item.categoryName}</span>
          <h1 className="text-2xl font-bold text-gray-900 mt-2">{item.name}</h1>
          <p className="text-gray-500 mt-2 text-sm leading-relaxed">{item.description || 'No description available.'}</p>
          {item.avgRating && (
            <div className="flex items-center gap-1 mt-3 text-amber-500">
              {'★'.repeat(Math.round(item.avgRating))}{'☆'.repeat(5-Math.round(item.avgRating))}
              <span className="text-gray-500 text-sm ml-1">({reviews.length} reviews)</span>
            </div>
          )}
          <div className="text-3xl font-bold text-cafe-700 mt-4">₱{Number(item.price).toFixed(2)}</div>
          <div className="flex items-center gap-4 mt-5">
            <div className="flex items-center border border-gray-200 rounded-lg overflow-hidden">
              <button onClick={() => setQty(q => Math.max(1,q-1))} className="px-3 py-2 hover:bg-gray-50">−</button>
              <span className="px-4 py-2 font-semibold">{qty}</span>
              <button onClick={() => setQty(q => q+1)} className="px-3 py-2 hover:bg-gray-50">+</button>
            </div>
            <button onClick={handleAdd} className="btn-primary flex-1 py-3">Add to Cart</button>
          </div>
        </div>
      </div>

      {/* Frequently Bought Together */}
      {similar.length > 0 && (
        <div className="mt-10">
          <h2 className="font-bold text-xl text-gray-900 mb-4">Frequently Bought Together</h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {similar.map(it => <RecommendationCard key={it.id} item={it} />)}
          </div>
        </div>
      )}

      {/* Reviews */}
      {reviews.length > 0 && (
        <div className="mt-8">
          <h2 className="font-bold text-lg text-gray-900 mb-4">Customer Reviews</h2>
          <div className="space-y-3">
            {reviews.map(r => (
              <div key={r.id} className="card p-4">
                <div className="flex items-center gap-2 mb-1">
                  <span className="text-amber-400">{'★'.repeat(r.rating)}{'☆'.repeat(5-r.rating)}</span>
                  <span className="text-sm font-medium text-gray-700">{r.customerName || 'Anonymous'}</span>
                </div>
                {r.review && <p className="text-sm text-gray-600">{r.review}</p>}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
