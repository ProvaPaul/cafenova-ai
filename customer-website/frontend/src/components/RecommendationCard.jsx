import { useCart } from '../contexts/CartContext'
import { useAuth } from '../contexts/AuthContext'
import { useNavigate, Link } from 'react-router-dom'
import toast from 'react-hot-toast'

/**
 * RecommendationCard — displays a single AI recommendation.
 *
 * Props:
 *   item: {
 *     recommendation: string   (item name from AI)
 *     confidence: float        (0–1)
 *     support: float           (0–1)
 *     lift: float              (>=1 means positively correlated)
 *     reason: string           (e.g. "Frequently Bought Together")
 *     id?: int                 (optional cafe menu item id)
 *     price?: float
 *   }
 *   showDemoBadge: bool (default true)
 *   onAdd?: function(item)     (optional override for cart add)
 */
export default function RecommendationCard({ item, showDemoBadge = true, onAdd }) {
  const { addItem } = useCart()
  const { user }    = useAuth()
  const navigate    = useNavigate()

  const confPct = Math.round((item.confidence ?? 0) * 100)
  const lift    = typeof item.lift === 'number' ? item.lift.toFixed(2) : null
  const support = typeof item.support === 'number' ? (item.support * 100).toFixed(1) : null
  const name    = item.recommendation || item.name || 'Unknown'
  const reason  = item.reason || 'Recommended for You'

  const handleAdd = async () => {
    if (onAdd) { onAdd(item); return }
    if (!user) { navigate('/login'); return }
    if (!item.id) {
      toast.success(`${name} — check menu to add`)
      return
    }
    try {
      await addItem(item.id, 1)
      toast.success(`${name} added to cart`)
    } catch {
      toast.error('Could not add item')
    }
  }

  return (
    <div className="card p-4 flex flex-col gap-2 hover:shadow-md transition-shadow relative">
      {/* Demo AI badge */}
      {showDemoBadge && (
        <span className="absolute top-2 right-2 text-[9px] bg-purple-100 text-purple-600 border border-purple-200 px-1.5 py-0.5 rounded-full font-medium">
          Demo AI
        </span>
      )}

      {/* Icon placeholder */}
      <div className="w-full h-20 bg-gradient-to-br from-cafe-100 to-cafe-200 rounded-lg flex items-center justify-center text-3xl">
        ☕
      </div>

      {/* Name */}
      <p className="font-semibold text-gray-900 text-sm leading-tight capitalize">{name}</p>

      {/* Reason badge */}
      <span className="text-xs bg-amber-50 text-amber-700 border border-amber-200 px-2 py-0.5 rounded-full w-fit">
        {reason}
      </span>

      {/* AI Metrics row */}
      <div className="flex gap-2 text-xs text-gray-500 flex-wrap">
        <span title="Confidence: how often this item was bought after the trigger items">
          Conf <strong className="text-gray-700">{confPct}%</strong>
        </span>
        {lift && (
          <span title="Lift: how much more likely than by chance (>1 = positive association)">
            Lift <strong className="text-gray-700">{lift}</strong>
          </span>
        )}
        {support && (
          <span title="Support: % of all baskets containing this item">
            Sup <strong className="text-gray-700">{support}%</strong>
          </span>
        )}
      </div>

      {/* Confidence bar */}
      <div className="flex items-center gap-2">
        <div className="flex-1 bg-gray-100 rounded-full h-1.5">
          <div
            className="bg-cafe-500 h-1.5 rounded-full transition-all"
            style={{ width: `${confPct}%` }}
          />
        </div>
        <span className="text-xs text-gray-400 w-8 text-right">{confPct}%</span>
      </div>

      {/* Add button or Browse link */}
      {item.id ? (
        <button onClick={handleAdd} className="btn-primary text-xs py-1.5 w-full mt-1">
          + Add to Cart
        </button>
      ) : (
        <Link to="/menu" className="block text-center text-xs text-cafe-600 hover:underline mt-1 py-1.5 border border-cafe-200 rounded-lg">
          Find in Menu
        </Link>
      )}
    </div>
  )
}
