import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import api from '../../api/client'
import toast from 'react-hot-toast'

export default function ReservationPage() {
  const [tables, setTables] = useState([])
  const [form, setForm] = useState({ tableId: '', partySize: 2, reservationDate: '', reservationTime: '12:00', notes: '' })
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    api.get('/reservations/tables').then(r => setTables(r.data.data || []))
  }, [])

  const f = k => e => setForm(p => ({...p, [k]: e.target.value}))

  const submit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await api.post('/reservations', form)
      toast.success('Reservation submitted! We will confirm shortly.')
      navigate('/reservations/my')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit reservation')
    } finally {
      setLoading(false)
    }
  }

  const today = new Date().toISOString().split('T')[0]

  return (
    <div className="max-w-2xl mx-auto px-4 py-10">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Reserve a Table</h1>
          <p className="text-gray-500 text-sm mt-1">Book your spot in advance</p>
        </div>
        <Link to="/reservations/my" className="text-sm text-cafe-600 hover:underline">My Reservations →</Link>
      </div>

      <div className="card p-6">
        <form onSubmit={submit} className="space-y-5">
          {/* Table */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Select Table</label>
            <select className="input-field" value={form.tableId} onChange={f('tableId')} required>
              <option value="">Choose a table…</option>
              {tables.map(t => (
                <option key={t.id} value={t.id}>
                  Table {t.tableNumber} — Seats {t.capacity} ({t.location})
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            {/* Date */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Date</label>
              <input className="input-field" type="date" min={today}
                value={form.reservationDate} onChange={f('reservationDate')} required />
            </div>
            {/* Time */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Time</label>
              <input className="input-field" type="time"
                value={form.reservationTime} onChange={f('reservationTime')} required />
            </div>
          </div>

          {/* Party size */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Party Size</label>
            <input className="input-field" type="number" min={1} max={20}
              value={form.partySize} onChange={f('partySize')} required />
          </div>

          {/* Notes */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Special Requests (optional)</label>
            <textarea className="input-field resize-none" rows={3} placeholder="Birthday celebration, wheelchair access, etc."
              value={form.notes} onChange={f('notes')} />
          </div>

          <button type="submit" disabled={loading} className="btn-primary w-full py-3">
            {loading ? 'Submitting…' : 'Submit Reservation'}
          </button>
        </form>
      </div>
    </div>
  )
}
