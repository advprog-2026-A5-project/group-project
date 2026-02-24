"use client";

import { useState, FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

export default function RegisterPage() {
    const router = useRouter();
    const [formData, setFormData] = useState({
        username: "",
        password: "",
        confirmPassword: "",
    });
    const [loading, setLoading] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleRegister = async (e: FormEvent) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            alert("Password dan Konfirmasi Password tidak cocok!");
            return;
        }

        setLoading(true);

        try {
            const response = await fetch("/api/auth/signup", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    username: formData.username,
                    password: formData.password,
                }),
            });

            const data = await response.json();

            if (response.ok) {
                if (data.token || data.accessToken) {
                    localStorage.setItem("token", data.token || data.accessToken);
                }

                alert("Registrasi Berhasil!");
                router.push("/login");
            } else {
                alert(`Gagal: ${data.message || "Terjadi kesalahan pada server"}`);
            }
        } catch (error) {
            console.error("Error pas manggil API:", error);
            alert("Koneksi gagal! Pastikan backend nyala dan proxy di next.config.ts sudah benar.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-[#FDF8F3] px-4 py-12">
            <div className="absolute top-0 left-0 w-full h-2 bg-[#8B4513]" />

            <div className="w-full max-w-md">
                <div className="bg-white rounded-2xl shadow-2xl overflow-hidden border border-[#EEDDCC]">

                    <div className="bg-[#8B4513] p-8 text-center">
                        <h2 className="text-3xl font-extrabold text-white tracking-tight">MySawit</h2>
                        <img src="/logo.png" alt="Logo" />
                        <p className="text-[#FDF8F3]/80 text-sm mt-2 font-medium">Buat akun baru untuk mulai memantau</p>
                    </div>

                    <div className="p-8">
                        <form className="space-y-5" onSubmit={handleRegister}>
                            <div className="space-y-1">
                                <label className="text-sm font-bold text-[#5D2E0B] ml-1">Username</label>
                                <input
                                    name="username"
                                    type="text"
                                    required
                                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all"
                                    placeholder="Masukkan username"
                                    value={formData.username}
                                    onChange={handleChange}
                                />
                            </div>

                            <div className="space-y-1">
                                <label className="text-sm font-bold text-[#5D2E0B] ml-1">Password</label>
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

                            <div className="space-y-1">
                                <label className="text-sm font-bold text-[#5D2E0B] ml-1">Konfirmasi Password</label>
                                <input
                                    name="confirmPassword"
                                    type="password"
                                    required
                                    className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-[#CD853F] outline-none transition-all"
                                    placeholder="Ulangi password"
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                />
                            </div>

                            <button
                                type="submit"
                                disabled={loading}
                                className={`w-full bg-[#D2691E] hover:bg-[#8B4513] text-white font-bold py-4 rounded-xl shadow-lg transition-all transform active:scale-[0.98] mt-4 ${loading ? "opacity-50 cursor-not-allowed" : ""
                                    }`}
                            >
                                {loading ? "MENGIRIM..." : "DAFTAR SEKARANG"}
                            </button>
                        </form>

                        <div className="relative my-8">
                            <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-gray-100"></div></div>
                            <div className="relative flex justify-center text-xs uppercase">
                                <span className="bg-white px-3 text-gray-400 font-medium">Sudah punya akun?</span>
                            </div>
                        </div>

                        <div className="text-center">
                            <Link
                                href="/login"
                                className="flex items-center justify-center w-full py-3 border-2 border-[#8B4513] text-[#8B4513] font-bold rounded-xl hover:bg-[#FDF8F3] transition-colors"
                            >
                                Masuk ke Akun
                            </Link>
                        </div>
                    </div>
                </div>
                <p className="text-center mt-8 text-xs text-[#8B4513]/50 font-medium">
                    &copy; 2026 MySawit Indonesia
                </p>
            </div>
        </div>
    );
}
