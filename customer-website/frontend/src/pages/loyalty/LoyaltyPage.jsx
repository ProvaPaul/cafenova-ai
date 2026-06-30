import { useState, useEffect } from 'react'
import { useAuth } from '../../contexts/AuthContext'
import api from '../../api/client'
import toast from 'react-hot-toast'

const TIERS = [
  { name: 'Bronze',   min: 0,    color: 'from-amber-600 to-amber-700' },
  { name: 'Silver',   min: 500,  color: 'from-gray-400 to-gray-500' },
  { name: 'Gold',     min: 2000, color: 'from-yellow-400 to-yellow-500' },
  { name: 'Platinum', min: 5000, color: 'from-sky-400 to-sky-600' },
]

function TierProgress({ points }) {
  const currentTierIdx = TIERS.reduce((acc, t, i) => points >= t.min ? i : acc, 0)
  const nextTier = TIERS[currentTierIdx + 1]
  const currentTier = TIERS[currentTierIdx]
  const progress = nextTier
    ? ((points - currentTier.min) / (nextTier.min - currentTier.min)) * 100
    : 100

  return (
    <div className="mb-4">
      <div className="flex justify-between text-sm text-gray-600 mb-1">
        <span>{currentTier.name}</span>
        {nextTier && <span>{nextTier.name} at {nextTier.min} pts</span>}
      </div>
      <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
        <div className="h-full bg-cafe-600 rounded-full transition-all" style={{ width: `${Math.min(progress, 100)}%` }} />
      </div>
      {nextTier && (
        <p className="text-xs text-gray-500 mt-1">{nextTier.min - points} points to {nextTier.name}</p>
      )}
    </div>
  )
}

export default function LoyaltyPage() {
  const { user, updateUser } = useAuth()
  const [coupons, setCoupons]   = useState([])
  const [history, setHistory]   = useState([])
  const [redeeming, setRedeeming] = useState(false)

  const points = user?.loyaltyPoints || 0
  const tier   = user?.loyaltyTier || 'Bronze'

  useEffect(() => {
    api.get('/loyalty/coupons').then(r => setCoupons(r.data.data || []))
    api.get('/loyalty/history').then(r => setHistory(r.data.data || []))
  }, [])

  const redeemPoints = async () => {
    if (points < 100) { toast.error('You need at least 100 points to redeem'); return }
    setRedeeming(true)
    try {
      const res = await api.post('/loyalty/redeem', { points: 100 })
      toast.success('Redeemed 100 points for a ₱50 coupon!')
      updateUser({ ...user, loyaltyPoints: points - 100 })
      setCoupons(p => [res.data.data, ...p])
    } catch (err) {
      toast.error(err.response?.data?.message || 'Redemption failed')
    } finally {
      setRedeeming(false)
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-10 space-y-6">
      {/* Points card */}
      <div className={`bg-gradient-to-br ${TIERS.find(t=>t.name===tier)?.color || 'from-amber-600 to-amber-700'} rounded-2xl p-6 text-white`}>
        <div className="flex items-center justify-between mb-4">
          <div>
            <p className="text-sm opacity-80">Your Points</p>
            <p className="text-4xl font-bold">{points.toLocaleString()}</p>
          </div>
          <div className="text-right">
            <p className="text-sm opacity-80">Tier</p>
            <p className="text-2xl font-bold">{tier}</p>
          </div>
        </div>
        <TierProgress points={points} />
        <p className="text-xs opacity-70 mt-2">Earn 1 point for every ₱10 spent · 100 points = ₱50 coupon</p>
      </div>

      {/* Redeem */}
      <div className="card p-5">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="font-semibold text-gray-900">Redeem Points</h2>
            <p className="text-sm text-gray-500 mt-0.5">Trade 100 points for a ₱50 coupon (valid 30 days)</p>
          </div>
          <button onClick={redeemPoints} disabled={redeeming || points < 100}
            className={`px-5 py-2 rounded-lg text-sm font-medium transition-colors ${points >= 100 ? 'btn-primary' : 'bg-gray-100 text-gray-400 cursor-not-allowed'}`}>
            {redeeming ? 'Redeeming…' : 'Redeem 100 pts'}
          </button>
        </div>
      </div>

      {/* Coupons */}
      {coupons.length > 0 && (
        <div className="card p-5">
          <h2 className="font-semibold text-gray-900 mb-4">My Coupons</h2>
          <div className="space-y-2">
            {coupons.map(c => (
              <div key={c.id} className={`flex items-center justify-between border rounded-lg px-4 py-3 ${c.isUsed ? 'bg-gray-50 border-gray-100' : 'bg-green-50 border-green-200'}`}>
                <div>
                  <span className="font-mono font-bold text-gray-800">{c.code}</span>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {c.discountType === 'PERCENTAGE' ? `${c.discountValue}% off` : `₱${c.discountValue} off`}
                    {c.expiresAt && ` · Expires ${new Date(c.expiresAt).toLocaleDateString()}`}
                  </p>
                </div>
                {c.isUsed ? (
                  <span className="text-xs text-gray-400 bg-gray-100 px-2 py-0.5 rounded-full">Used</span>
                ) : (
                  <span className="text-xs text-green-700 bg-green-100 px-2 py-0.5 rounded-full font-medium">Active</span>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* History */}
      {history.length > 0 && (
        <div className="card p-5">
          <h2 className="font-semibold text-gray-900 mb-4">Points History</h2>
          <div className="space-y-2">
            {history.map(h => (
              <div key={h.id} className="flex items-center justify-between text-sm">
                <span className="text-gray-600">{h.description} · <span className="text-gray-400">{new Date(h.createdAt).toLocaleDateString()}</span></span>
                <span className={`font-semibold ${h.points > 0 ? 'text-green-600' : 'text-red-500'}`}>
                  {h.points > 0 ? '+' : ''}{h.points} pts
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
