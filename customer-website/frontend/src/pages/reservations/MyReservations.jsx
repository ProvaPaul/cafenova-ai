import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../../api/client'
import toast from 'react-hot-toast'

const STATUS_COLOR = {
  PENDING:   'bg-yellow-100 text-yellow-700',
  CONFIRMED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-600',
  COMPLETED: 'bg-gray-100 text-gray-600',
  NO_SHOW:   'bg-orange-100 text-orange-600',
}

export default function MyReservations() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)

  const fetchData = () => {
    api.get('/reservations/my').then(r => { setReservations(r.data.data || []); setLoading(false) })
  }

  useEffect(fetchData, [])

  const cancel = async (id) => {
    if (!window.confirm('Cancel this reservation?')) return
    try {
      await api.patch(`/reservations/${id}/cancel`)
      toast.success('Reservation cancelled')
      fetchData()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Cannot cancel')
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-10">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">My Reservations</h1>
        <Link to="/reservations" className="btn-primary text-sm px-4 py-2">+ New Reservation</Link>
      </div>

      {loading ? (
        <div className="space-y-3">{[...Array(3)].map((_,i)=><div key={i} className="card h-20 animate-pulse bg-gray-100"/>)}</div>
      ) : reservations.length === 0 ? (
        <div className="text-center py-20 text-gray-400">
          <div className="text-5xl mb-4">📅</div>
          <p className="font-medium">No reservations yet</p>
          <Link to="/reservations" className="btn-primary inline-block mt-4 px-6">Make a Reservation</Link>
        </div>
      ) : (
        <div className="space-y-4">
          {reservations.map(res => (
            <div key={res.id} className="card p-5 flex items-center gap-4">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLOR[res.status] || 'bg-gray-100 text-gray-600'}`}>
                    {res.status}
                  </span>
                </div>
                <p className="font-semibold text-gray-900">
                  {new Date(res.reservationDate).toLocaleDateString('en-PH', { weekday:'long', year:'numeric', month:'long', day:'numeric' })} at {res.reservationTime}
                </p>
                <p className="text-sm text-gray-500">Party of {res.partySize}{res.notes ? ` · ${res.notes}` : ''}</p>
              </div>
              {(res.status === 'PENDING' || res.status === 'CONFIRMED') && (
                <button onClick={() => cancel(res.id)}
                  className="text-sm text-red-500 hover:underline whitespace-nowrap flex-shrink-0">
                  Cancel
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
