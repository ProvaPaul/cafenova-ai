import { useState, useEffect } from 'react'
import api from '../../api/client'
import ProductCard from '../../components/ProductCard'

export default function MenuPage() {
  const [items, setItems]       = useState([])
  const [categories, setCategories] = useState([])
  const [selectedCat, setSelectedCat] = useState(null)
  const [search, setSearch]     = useState('')
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    api.get('/categories').then(r => setCategories(r.data.data || []))
  }, [])

  useEffect(() => {
    setLoading(true)
    const params = new URLSearchParams()
    if (search) params.set('q', search)
    else if (selectedCat) params.set('categoryId', selectedCat)
    api.get(`/menu?${params}`).then(r => { setItems(r.data.data || []); setLoading(false) })
  }, [search, selectedCat])

  return (
    <div className="max-w-7xl mx-auto px-4 py-10">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-1">Our Menu</h1>
        <p className="text-gray-500">Freshly prepared with quality ingredients</p>
      </div>

      {/* Search */}
      <div className="relative mb-6 max-w-md">
        <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <input className="input-field pl-10" placeholder="Search menu…"
          value={search} onChange={e => { setSearch(e.target.value); setSelectedCat(null) }} />
      </div>

      {/* Category filters */}
      {!search && (
        <div className="flex gap-2 flex-wrap mb-8">
          <button onClick={() => setSelectedCat(null)}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${!selectedCat ? 'bg-cafe-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
            All
          </button>
          {categories.map(c => (
            <button key={c.id} onClick={() => setSelectedCat(c.id)}
              className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${selectedCat === c.id ? 'bg-cafe-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
              {c.name}
            </button>
          ))}
        </div>
      )}

      {loading ? (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {[...Array(8)].map((_, i) => (
            <div key={i} className="card h-72 animate-pulse bg-gray-100" />
          ))}
        </div>
      ) : items.length === 0 ? (
        <div className="text-center py-20 text-gray-400">
          <div className="text-5xl mb-4">😔</div>
          <p className="font-medium">No items found</p>
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {items.map(item => <ProductCard key={item.id} item={item} />)}
        </div>
      )}
    </div>
  )
}
