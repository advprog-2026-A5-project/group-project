"use client";

import { useState, FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function LoginPage() {
    const router = useRouter();
    const [formData, setFormData] = useState({
        username: "",
        password: "",
    });
    const [loading, setLoading] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleLogin = async (e: FormEvent) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await fetch("http://localhost:8080/api/auth/signin", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    username: formData.username,
                    password: formData.password,
                }),
            });

            if (response.ok) {
                const data = await response.json();
                // Simpan token atau data user di sini jika ada (misal di localStorage)
                console.log("Login Berhasil:", data);
                alert("Selamat Datang di MySawit!");
                router.push("/dashboard"); // Redirect ke halaman utama setelah login
            } else {
                const errorData = await response.json();
                alert(`Gagal Masuk: ${errorData.message || "Username atau password salah"}`);
            }
        } catch (error) {
            console.error("Network Error:", error);
            alert("Koneksi gagal! Pastikan backend MySawit sudah jalan di port 8080.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-[#FDF8F3] px-4">
            {}
            <div className="absolute top-0 left-0 w-full h-2 bg-[#8B4513]" />

            <div className="w-full max-w-md">
                <div className="bg-white rounded-2xl shadow-2xl overflow-hidden border border-[#EEDDCC]">

                    <div className="bg-[#8B4513] p-10 text-center">
                        <h2 className="text-3xl font-extrabold text-white tracking-tight">MySawit</h2>
                        <p className="text-[#FDF8F3]/80 text-sm mt-2 font-medium">Masuk untuk memantau hasil kebun</p>
                    </div>

                    <div className="p-8">
                        <form className="space-y-6" onSubmit={handleLogin}>
                            {/* Input Username */}
                            <div className="space-y-1">
                                <label className="text-sm font-bold text-[#5D2E0B] ml-1">Username</label>
                                <input
                                    name="username"
                                    type="text"
                                    required
                                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all"
                                    placeholder="Username Anda"
                                    value={formData.username}
                                    onChange={handleChange}
                                />
                            </div>

                            {/* Input Password */}
                            <div className="space-y-1">
                                <div className="flex justify-between items-center ml-1">
                                    <label className="text-sm font-bold text-[#5D2E0B]">Password</label>
                                    <a href="#" className="text-xs text-[#D2691E] font-semibold hover:underline">Lupa Password?</a>
                                </div>
                                <input
                                    name="password"
                                    type="password"
                                    required
                                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all"
                                    placeholder="••••••••"
                                    value={formData.password}
                                    onChange={handleChange}
                                />
                            </div>

                            <button
                                type="submit"
                                disabled={loading}
                                className={`w-full bg-[#8B4513] hover:bg-[#703810] text-white font-bold py-4 rounded-xl shadow-lg transition-all transform active:scale-[0.98] ${
                                    loading ? "opacity-50 cursor-not-allowed" : ""
                                }`}
                            >
                                {loading ? "MENCOBA MASUK..." : "MASUK"}
                            </button>
                        </form>

                        <div className="relative my-8">
                            <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-gray-100"></div></div>
                            <div className="relative flex justify-center text-xs uppercase">
                                <span className="bg-white px-3 text-gray-400 font-medium">Atau</span>
                            </div>
                        </div>

                        <div className="text-center">
                            <p className="text-sm text-gray-500 mb-4">Belum punya akun MySawit?</p>
                            <Link
                                href="/register"
                                className="flex items-center justify-center w-full py-3 border-2 border-[#D2691E] text-[#D2691E] font-bold rounded-xl hover:bg-[#FFF5EB] transition-colors"
                            >
                                Daftar Akun Baru
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}