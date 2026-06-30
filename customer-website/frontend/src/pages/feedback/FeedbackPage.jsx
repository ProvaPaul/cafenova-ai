import { useState, useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import api from '../../api/client'
import toast from 'react-hot-toast'

function StarPicker({ value, onChange }) {
  const [hovered, setHovered] = useState(0)
  return (
    <div className="flex gap-1">
      {[1,2,3,4,5].map(n => (
        <button type="button" key={n}
          onMouseEnter={() => setHovered(n)} onMouseLeave={() => setHovered(0)}
          onClick={() => onChange(n)}
          className="text-3xl transition-colors focus:outline-none">
          <span className={(hovered || value) >= n ? 'text-amber-400' : 'text-gray-200'}>★</span>
        </button>
      ))}
    </div>
  )
}

export default function FeedbackPage() {
  const location  = useLocation()
  const navigate  = useNavigate()
  const [menuItems, setMenuItems] = useState([])
  const [form, setForm] = useState({
    menuItemId: '',
    rating: 0,
    review: '',
    orderId: location.state?.orderId || '',
  })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    api.get('/menu?pageSize=100').then(r => setMenuItems(r.data.data || []))
  }, [])

  const submit = async (e) => {
    e.preventDefault()
    if (form.rating === 0) { toast.error('Please select a rating'); return }
    setLoading(true)
    try {
      await api.post('/feedback', form)
      toast.success('Thank you for your feedback!')
      navigate('/orders')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-xl mx-auto px-4 py-10">
      <h1 className="text-2xl font-bold text-gray-900 mb-2">Leave a Review</h1>
      <p className="text-gray-500 mb-6">Help us improve by sharing your experience</p>

      <div className="card p-6">
        <form onSubmit={submit} className="space-y-5">
          {/* Item */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Menu Item</label>
            <select className="input-field" value={form.menuItemId}
              onChange={e => setForm(p => ({...p, menuItemId: e.target.value}))} required>
              <option value="">Select an item…</option>
              {menuItems.map(item => (
                <option key={item.id} value={item.id}>{item.name}</option>
              ))}
            </select>
          </div>

          {/* Rating */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Your Rating</label>
            <StarPicker value={form.rating} onChange={r => setForm(p => ({...p, rating: r}))} />
            {form.rating > 0 && (
              <p className="text-sm text-gray-500 mt-1">
                {['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent'][form.rating]}
              </p>
            )}
          </div>

          {/* Review */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Review (optional)</label>
            <textarea className="input-field resize-none" rows={4}
              placeholder="Tell us about your experience with this item…"
              value={form.review} onChange={e => setForm(p => ({...p, review: e.target.value}))} />
          </div>

          <button type="submit" disabled={loading} className="btn-primary w-full py-3">
            {loading ? 'Submitting…' : 'Submit Review'}
          </button>
        </form>
      </div>
    </div>
  )
}
