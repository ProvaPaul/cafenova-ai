import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'
import toast from 'react-hot-toast'

export default function Register() {
  const [form, setForm] = useState({ fullName: '', username: '', email: '', password: '', phone: '' })
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const f = k => e => setForm(p => ({...p, [k]: e.target.value}))

  const submit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await register(form)
      toast.success('Account created! Welcome!')
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-[80vh] flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <span className="text-5xl">☕</span>
          <h1 className="text-2xl font-bold text-gray-900 mt-3">Create an account</h1>
          <p className="text-gray-500 mt-1">Join the Smart Cafe family</p>
        </div>
        <div className="card p-8">
          <form onSubmit={submit} className="space-y-4">
            {[
              { label: 'Full Name', key: 'fullName', type: 'text', placeholder: 'Juan Dela Cruz' },
              { label: 'Username',  key: 'username', type: 'text', placeholder: 'juandelacruz' },
              { label: 'Email',     key: 'email',    type: 'email', placeholder: 'juan@email.com' },
              { label: 'Phone',     key: 'phone',    type: 'tel',  placeholder: '09171234567', required: false },
              { label: 'Password',  key: 'password', type: 'password', placeholder: 'At least 6 characters' },
            ].map(field => (
              <div key={field.key}>
                <label className="block text-sm font-medium text-gray-700 mb-1">{field.label}</label>
                <input className="input-field" type={field.type} placeholder={field.placeholder}
                  value={form[field.key]} onChange={f(field.key)}
                  required={field.required !== false} minLength={field.key === 'password' ? 6 : undefined} />
              </div>
            ))}
            <button type="submit" disabled={loading} className="btn-primary w-full py-3 mt-2">
              {loading ? 'Creating account…' : 'Create Account'}
            </button>
          </form>
          <p className="text-center mt-4 text-sm text-gray-500">
            Already have an account?{' '}
            <Link to="/login" className="text-cafe-600 font-medium hover:underline">Sign in</Link>
          </p>
        </div>
      </div>
    </div>
  )
}
