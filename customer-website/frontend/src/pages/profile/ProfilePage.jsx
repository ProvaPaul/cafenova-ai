import { useState } from 'react'
import { useAuth } from '../../contexts/AuthContext'
import api from '../../api/client'
import toast from 'react-hot-toast'

export default function ProfilePage() {
  const { user, updateUser } = useAuth()
  const [form, setForm]     = useState({ fullName: user?.fullName || '', email: user?.email || '', phone: user?.phone || '' })
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [loading, setLoading]   = useState(false)
  const [pwLoading, setPwLoading] = useState(false)

  const f  = k => e => setForm(p => ({...p, [k]: e.target.value}))
  const fp = k => e => setPwForm(p => ({...p, [k]: e.target.value}))

  const saveProfile = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await api.put('/profile', form)
      updateUser(res.data.data)
      toast.success('Profile updated')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Update failed')
    } finally {
      setLoading(false)
    }
  }

  const changePassword = async (e) => {
    e.preventDefault()
    if (pwForm.newPassword !== pwForm.confirmPassword) { toast.error("Passwords don't match"); return }
    setPwLoading(true)
    try {
      await api.put('/profile/password', { currentPassword: pwForm.currentPassword, newPassword: pwForm.newPassword })
      toast.success('Password changed successfully')
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    } catch (err) {
      toast.error(err.response?.data?.message || 'Password change failed')
    } finally {
      setPwLoading(false)
    }
  }

  const TIER_COLOR = { Bronze: 'text-amber-700', Silver: 'text-gray-500', Gold: 'text-yellow-500', Platinum: 'text-sky-500' }

  return (
    <div className="max-w-3xl mx-auto px-4 py-10 space-y-6">
      {/* Header card */}
      <div className="card p-6 flex items-center gap-5">
        <div className="w-16 h-16 rounded-full bg-cafe-100 flex items-center justify-center text-3xl font-bold text-cafe-700">
          {user?.fullName?.[0]?.toUpperCase() || '?'}
        </div>
        <div>
          <h1 className="text-xl font-bold text-gray-900">{user?.fullName}</h1>
          <p className="text-sm text-gray-500">@{user?.username}</p>
          <p className={`text-sm font-semibold mt-0.5 ${TIER_COLOR[user?.loyaltyTier] || 'text-gray-500'}`}>
            {user?.loyaltyTier || 'Bronze'} Member · {user?.loyaltyPoints || 0} pts
          </p>
        </div>
      </div>

      {/* Profile form */}
      <div className="card p-6">
        <h2 className="font-semibold text-gray-900 mb-4">Personal Information</h2>
        <form onSubmit={saveProfile} className="space-y-4">
          {[
            { label: 'Full Name', key: 'fullName', type: 'text' },
            { label: 'Email',     key: 'email',    type: 'email' },
            { label: 'Phone',     key: 'phone',    type: 'tel' },
          ].map(field => (
            <div key={field.key}>
              <label className="block text-sm font-medium text-gray-700 mb-1">{field.label}</label>
              <input className="input-field" type={field.type} value={form[field.key]} onChange={f(field.key)} />
            </div>
          ))}
          <button type="submit" disabled={loading} className="btn-primary px-6 py-2">
            {loading ? 'Saving…' : 'Save Changes'}
          </button>
        </form>
      </div>

      {/* Password change */}
      <div className="card p-6">
        <h2 className="font-semibold text-gray-900 mb-4">Change Password</h2>
        <form onSubmit={changePassword} className="space-y-4">
          {[
            { label: 'Current Password', key: 'currentPassword' },
            { label: 'New Password',     key: 'newPassword' },
            { label: 'Confirm New Password', key: 'confirmPassword' },
          ].map(field => (
            <div key={field.key}>
              <label className="block text-sm font-medium text-gray-700 mb-1">{field.label}</label>
              <input className="input-field" type="password" value={pwForm[field.key]} onChange={fp(field.key)} required minLength={6} />
            </div>
          ))}
          <button type="submit" disabled={pwLoading} className="btn-secondary px-6 py-2">
            {pwLoading ? 'Changing…' : 'Change Password'}
          </button>
        </form>
      </div>
    </div>
  )
}
