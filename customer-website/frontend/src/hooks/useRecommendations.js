import { useState, useEffect, useCallback } from 'react'
import api from '../api/client'

/**
 * Fetches AI recommendations from the Spring Boot proxy -> FastAPI.
 *
 * The new /recommend endpoint accepts:
 *   { items: string[], limit: number, min_confidence: number, min_lift: number }
 * and returns:
 *   { recommendations: [{ recommendation, confidence, support, lift, reason }] }
 *
 * For the "trending" context (no input items), it calls GET /recommendations/trending.
 *
 * @param {'trending'|'recommend'|'cart'|'pos'} context
 * @param {object} params
 *   trending: { limit }
 *   recommend: { items, limit }
 *   cart:     { productNames, limit }
 *   pos:      { itemNames, limit }
 */
export function useRecommendations(context, params = {}) {
  const [items, setItems]     = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError]     = useState(null)
  const [demoNotice, setDemoNotice] = useState(null)

  const paramsKey = JSON.stringify(params)

  const fetch = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      let resp
      if (context === 'trending') {
        resp = await api.get('/recommendations/trending', { params: { limit: params.limit || 5 } })
      } else if (context === 'recommend') {
        resp = await api.post('/recommendations/recommend', {
          items:          params.items || [],
          limit:          params.limit || 5,
          min_confidence: params.minConfidence || 0.30,
          min_lift:       params.minLift || 1.0,
        })
      } else if (context === 'cart') {
        resp = await api.post('/recommendations/cart', {
          product_ids:   params.productIds   || [],
          product_names: params.productNames || [],
          limit:         params.limit || 5,
        })
      } else if (context === 'pos') {
        resp = await api.post('/recommendations/pos', {
          current_item_ids:   params.itemIds   || [],
          current_item_names: params.itemNames || [],
          limit:              params.limit || 4,
        })
      } else if (context === 'personal') {
        resp = await api.get('/recommendations/personal', {
          params: { limit: params.limit || 5, context: params.ctx || 'home' },
        })
      }

      const data = resp?.data?.data || resp?.data || {}
      // Support both new RecommendResponse and legacy RecommendationResponse shapes
      const recs = data?.recommendations || []
      setItems(recs)
      if (data?.demo_notice) setDemoNotice(data.demo_notice)
    } catch (e) {
      setError(e)
      setItems([])
    } finally {
      setLoading(false)
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [context, paramsKey])

  useEffect(() => { fetch() }, [fetch])

  return { items, loading, error, demoNotice, refetch: fetch }
}
