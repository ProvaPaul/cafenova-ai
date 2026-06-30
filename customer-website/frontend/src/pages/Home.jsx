import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function Home() {
  const { user } = useAuth()

  return (
    <div>
      {/* Hero */}
      <section className="relative bg-gradient-to-br from-cafe-900 via-cafe-800 to-cafe-700 text-white py-24 px-4">
        <div className="max-w-4xl mx-auto text-center">
          <div className="text-6xl mb-6">☕</div>
          <h1 className="text-5xl font-bold mb-4 leading-tight">
            Your Favourite Cafe,<br />Now Online
          </h1>
          <p className="text-cafe-200 text-xl mb-8 max-w-2xl mx-auto">
            Order your favourite drinks and food, reserve a table, and earn loyalty points with every visit.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/menu" className="bg-white text-cafe-800 px-8 py-3 rounded-xl font-semibold hover:bg-cafe-50 transition-colors text-lg">
              Browse Menu
            </Link>
            {!user && (
              <Link to="/register" className="border-2 border-white text-white px-8 py-3 rounded-xl font-semibold hover:bg-white hover:text-cafe-800 transition-colors text-lg">
                Join Now — It's Free
              </Link>
            )}
            {user && (
              <Link to="/reservations" className="border-2 border-white text-white px-8 py-3 rounded-xl font-semibold hover:bg-white hover:text-cafe-800 transition-colors text-lg">
                Reserve a Table
              </Link>
            )}
          </div>
        </div>
      </section>

      {/* Features */}
      <section className="max-w-7xl mx-auto px-4 py-16">
        <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">Why Order with Us?</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {[
            { icon: '🛒', title: 'Easy Online Ordering', desc: 'Browse our full menu, add to cart, and checkout in minutes. Your order goes straight to our kitchen.' },
            { icon: '⭐', title: 'Loyalty Rewards', desc: 'Earn 1 point for every ₱10 spent. Redeem points for free drinks and exclusive discounts.' },
            { icon: '📅', title: 'Table Reservations', desc: "Reserve your favourite table online. We'll have it ready when you arrive." },
          ].map(f => (
            <div key={f.title} className="card p-6 text-center">
              <div className="text-4xl mb-4">{f.icon}</div>
              <h3 className="font-semibold text-lg text-gray-900 mb-2">{f.title}</h3>
              <p className="text-gray-500 text-sm">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Loyalty tiers */}
      <section className="bg-cafe-50 py-16 px-4">
        <div className="max-w-5xl mx-auto text-center">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">Loyalty Tiers</h2>
          <p className="text-gray-500 mb-10">The more you order, the more you earn.</p>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              { tier: 'Bronze',   pts: '0',    color: 'from-amber-600 to-amber-700' },
              { tier: 'Silver',   pts: '500',  color: 'from-gray-400 to-gray-500' },
              { tier: 'Gold',     pts: '2,000',color: 'from-yellow-400 to-yellow-500' },
              { tier: 'Platinum', pts: '5,000',color: 'from-sky-400 to-sky-600' },
            ].map(t => (
              <div key={t.tier} className={`bg-gradient-to-br ${t.color} rounded-xl p-5 text-white`}>
                <div className="text-2xl font-bold mb-1">{t.tier}</div>
                <div className="text-sm opacity-80">{t.pts}+ pts</div>
              </div>
            ))}
          </div>
          <Link to={user ? '/loyalty' : '/register'} className="inline-block mt-8 btn-primary px-8 py-3 text-base">
            {user ? 'View My Points' : 'Sign Up & Start Earning'}
          </Link>
        </div>
      </section>
    </div>
  )
}
