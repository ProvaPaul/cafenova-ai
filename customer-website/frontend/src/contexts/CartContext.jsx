import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import api from '../api/client'
import { useAuth } from './AuthContext'

const CartContext = createContext(null)

export function CartProvider({ children }) {
  const { user } = useAuth()
  const [cart, setCart] = useState([])
  const [loading, setLoading] = useState(false)

  const fetchCart = useCallback(async () => {
    if (!user) { setCart([]); return }
    try {
      const res = await api.get('/cart')
      setCart(res.data.data || [])
    } catch { setCart([]) }
  }, [user])

  useEffect(() => { fetchCart() }, [fetchCart])

  const addItem = useCallback(async (menuItemId, quantity = 1) => {
    await api.post('/cart/add', { menuItemId, quantity })
    await fetchCart()
  }, [fetchCart])

  const updateQuantity = useCallback(async (cartItemId, quantity) => {
    await api.put(`/cart/${cartItemId}`, { quantity })
    await fetchCart()
  }, [fetchCart])

  const removeItem = useCallback(async (cartItemId) => {
    await api.delete(`/cart/${cartItemId}`)
    await fetchCart()
  }, [fetchCart])

  const clearCart = useCallback(async () => {
    await api.delete('/cart')
    setCart([])
  }, [])

  const total = cart.reduce((sum, item) =>
    sum + (item.menuItem?.price || 0) * item.quantity, 0)

  const itemCount = cart.reduce((sum, item) => sum + item.quantity, 0)

  return (
    <CartContext.Provider value={{ cart, loading, total, itemCount, addItem, updateQuantity, removeItem, clearCart, fetchCart }}>
      {children}
    </CartContext.Provider>
  )
}

export const useCart = () => useContext(CartContext)
