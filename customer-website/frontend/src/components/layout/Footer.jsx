import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="bg-cafe-950 text-gray-300 mt-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="col-span-1 md:col-span-2">
            <div className="flex items-center gap-2 mb-4">
              <span className="text-2xl">☕</span>
              <span className="font-bold text-xl text-white">Smart Cafe</span>
            </div>
            <p className="text-sm text-gray-400 max-w-xs">
              Your favourite cafe, now online. Order your favourite drinks and food from the comfort of your home.
            </p>
          </div>
          <div>
            <h3 className="text-white font-semibold mb-3">Quick Links</h3>
            <ul className="space-y-2 text-sm">
              <li><Link to="/menu"         className="hover:text-cafe-300 transition-colors">Menu</Link></li>
              <li><Link to="/reservations" className="hover:text-cafe-300 transition-colors">Reserve a Table</Link></li>
              <li><Link to="/loyalty"      className="hover:text-cafe-300 transition-colors">Loyalty Program</Link></li>
            </ul>
          </div>
          <div>
            <h3 className="text-white font-semibold mb-3">My Account</h3>
            <ul className="space-y-2 text-sm">
              <li><Link to="/orders"  className="hover:text-cafe-300 transition-colors">Order History</Link></li>
              <li><Link to="/profile" className="hover:text-cafe-300 transition-colors">Profile</Link></li>
              <li><Link to="/feedback" className="hover:text-cafe-300 transition-colors">Leave Feedback</Link></li>
            </ul>
          </div>
        </div>
        <hr className="border-cafe-800 my-8" />
        <p className="text-center text-sm text-gray-500">
          © {new Date().getFullYear()} Smart Cafe Management System. All rights reserved.
        </p>
      </div>
    </footer>
  )
}
