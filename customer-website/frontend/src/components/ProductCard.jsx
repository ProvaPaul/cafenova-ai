import { Link } from 'react-router-dom'
import { useCart } from '../contexts/CartContext'
import { useAuth } from '../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'

export default function ProductCard({ item }) {
  const { addItem } = useCart()
  const { user } = useAuth()
  const navigate = useNavigate()

  const handleAdd = async (e) => {
    e.preventDefault()
    if (!user) { navigate('/login'); return }
    try {
      await addItem(item.id, 1)
      toast.success(`${item.name} added to cart`)
    } catch {
      toast.error('Could not add item')
    }
  }

  return (
    <Link to={`/menu/${item.id}`} className="card hover:shadow-md transition-shadow group flex flex-col">
      <div className="h-40 bg-gradient-to-br from-cafe-100 to-cafe-200 rounded-t-xl flex items-center justify-center text-5xl">
        ☕
      </div>
      <div className="p-4 flex flex-col flex-1">
        <div className="flex items-start justify-between gap-2 mb-1">
          <h3 className="font-semibold text-gray-900 group-hover:text-cafe-700 transition-colors">{item.name}</h3>
          <span className="font-bold text-cafe-600 whitespace-nowrap">₱{Number(item.price).toFixed(2)}</span>
        </div>
        {item.categoryName && (
          <span className="text-xs text-cafe-600 bg-cafe-50 px-2 py-0.5 rounded-full w-fit mb-2">{item.categoryName}</span>
        )}
        <p className="text-sm text-gray-500 line-clamp-2 flex-1">{item.description || 'No description'}</p>
        {item.avgRating && (
          <div className="flex items-center gap-1 mt-2 text-sm text-amber-500">
            <span>★</span><span className="font-medium">{item.avgRating}</span>
          </div>
        )}
        <button onClick={handleAdd}
          className="mt-3 btn-primary w-full text-sm py-1.5 rounded-lg">
          Add to Cart
        </button>
      </div>
    </Link>
  )
}
