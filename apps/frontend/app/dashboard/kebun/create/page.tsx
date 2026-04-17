"use client";

import { useState, FormEvent } from "react";
import { useRouter } from "next/navigation";

interface Coordinate {
  latitude: string;
  longitude: string;
}

export default function CreateKebunPage() {
  const router = useRouter();
  const [nama, setNama] = useState("");
  const [kodeKebun, setKodeKebun] = useState("");
  const [luas, setLuas] = useState("");
  const [coords, setCoords] = useState<Coordinate[]>([
    { latitude: "", longitude: "" },
    { latitude: "", longitude: "" },
    { latitude: "", longitude: "" },
    { latitude: "", longitude: "" },
  ]);
  const [loading, setLoading] = useState(false);

  const updateCoord = (index: number, field: "latitude" | "longitude", value: string) => {
    const newCoords = [...coords];
    newCoords[index] = { ...newCoords[index], [field]: value };
    setCoords(newCoords);
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const koordinat = coords.map((c) => ({
        latitude: parseFloat(c.latitude),
        longitude: parseFloat(c.longitude),
      }));

      // Validate all coordinates are numbers
      for (const k of koordinat) {
        if (isNaN(k.latitude) || isNaN(k.longitude)) {
          alert("Semua koordinat harus berupa angka yang valid");
          setLoading(false);
          return;
        }
      }

      const token = localStorage.getItem("token");
      const res = await fetch("/api/kebun", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ 
          nama, 
          kodeKebun, 
          luas: parseFloat(luas), 
          koordinat 
        }),
      });

      if (!res.ok) {
        const errText = await res.text();
        throw new Error(errText || "Gagal membuat kebun");
      }

      alert("Kebun berhasil dibuat!");
      router.push("/dashboard");
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : "Gagal membuat kebun");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <div className="mb-6">
        <a
          href="/dashboard"
          className="text-[#D2691E] hover:text-[#8B4513] font-medium text-sm flex items-center gap-1 transition-colors"
        >
          ← Kembali ke Daftar
        </a>
      </div>

      <div className="bg-white rounded-2xl shadow-md overflow-hidden border border-[#EEDDCC]">
        <div className="bg-gradient-to-r from-[#8B4513] to-[#D2691E] p-8">
          <h2 className="text-2xl font-bold text-white">Tambah Kebun Baru</h2>
          <p className="text-white/70 mt-1">
            Masukkan nama dan 4 titik koordinat area kebun
          </p>
        </div>

        <form onSubmit={handleSubmit} className="p-8 space-y-6">
          {/* Nama */}
          <div className="space-y-1">
            <label className="text-sm font-bold text-[#5D2E0B] ml-1">
              Nama Kebun
            </label>
            <input
              type="text"
              value={nama}
              onChange={(e) => setNama(e.target.value)}
              required
              className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-gray-900"
              placeholder="Contoh: Kebun Sawit Blok A"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            {/* Kode Kebun */}
            <div className="space-y-1">
              <label className="text-sm font-bold text-[#5D2E0B] ml-1">
                Kode Kebun
              </label>
              <input
                type="text"
                value={kodeKebun}
                onChange={(e) => setKodeKebun(e.target.value)}
                required
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-gray-900"
                placeholder="Contoh: KBN-01"
              />
            </div>

            {/* Luas */}
            <div className="space-y-1">
              <label className="text-sm font-bold text-[#5D2E0B] ml-1">
                Luas (Hektar)
              </label>
              <input
                type="number"
                step="any"
                value={luas}
                onChange={(e) => setLuas(e.target.value)}
                required
                min="0.1"
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-gray-900"
                placeholder="Contoh: 15.5"
              />
            </div>
          </div>

          {/* Coordinates */}
          <div className="space-y-4">
            <label className="text-sm font-bold text-[#5D2E0B] ml-1">
              Koordinat (4 Titik)
            </label>
            {coords.map((coord, i) => (
              <div
                key={i}
                className="flex gap-3 items-center bg-[#FDF8F3] p-4 rounded-xl border border-[#EEDDCC]"
              >
                <span className="w-8 h-8 bg-[#D2691E] text-white rounded-lg flex items-center justify-center font-bold text-sm flex-shrink-0">
                  {i + 1}
                </span>
                <div className="flex-1 grid grid-cols-2 gap-3">
                  <input
                    type="text"
                    value={coord.latitude}
                    onChange={(e) => updateCoord(i, "latitude", e.target.value)}
                    required
                    className="px-3 py-2 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-sm text-gray-900"
                    placeholder="Latitude (mis: -6.123)"
                  />
                  <input
                    type="text"
                    value={coord.longitude}
                    onChange={(e) => updateCoord(i, "longitude", e.target.value)}
                    required
                    className="px-3 py-2 rounded-lg border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all text-sm text-gray-900"
                    placeholder="Longitude (mis: 106.456)"
                  />
                </div>
              </div>
            ))}
          </div>

          {/* Submit */}
          <button
            type="submit"
            disabled={loading}
            className={`w-full bg-gradient-to-r from-[#8B4513] to-[#D2691E] text-white font-bold py-4 rounded-xl shadow-lg transition-all transform ${
              loading
                ? "opacity-50 cursor-not-allowed"
                : "hover:shadow-xl hover:scale-[1.01] active:scale-[0.99]"
            }`}
          >
            {loading ? "Menyimpan..." : "Simpan Kebun"}
          </button>
        </form>
      </div>
    </div>
  );
}
