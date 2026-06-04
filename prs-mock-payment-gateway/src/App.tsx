import { BrowserRouter, Routes, Route } from 'react-router-dom'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/payment" element={<div>Payment Gateway</div>} />
        <Route path="/" element={<div>Payment Gateway</div>} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
