import { createContext, useContext, useState, useCallback } from 'react'
import api from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('cafe_user')) } catch { return null }
  })

  const login = useCallback(async (username, password) => {
    const res = await api.post('/auth/login', { username, password })
    const data = res.data.data
    localStorage.setItem('cafe_token', data.token)
    localStorage.setItem('cafe_user', JSON.stringify(data))
    setUser(data)
    return data
  }, [])

  const register = useCallback(async (form) => {
    const res = await api.post('/auth/register', form)
    const data = res.data.data
    localStorage.setItem('cafe_token', data.token)
    localStorage.setItem('cafe_user', JSON.stringify(data))
    setUser(data)
    return data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('cafe_token')
    localStorage.removeItem('cafe_user')
    setUser(null)
  }, [])

  const updateUser = useCallback((updates) => {
    setUser(prev => {
      const next = { ...prev, ...updates }
      localStorage.setItem('cafe_user', JSON.stringify(next))
      return next
    })
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, register, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
