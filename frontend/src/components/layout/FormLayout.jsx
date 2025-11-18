import React from 'react'
import { cn } from '@/lib/utils'

function FormLayout({
  title,
  description,
  eyebrow,
  children,
  side,
  footer,
  className,
}) {
  return (
    <div className="relative isolate flex min-h-screen w-full items-center justify-center overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 px-6 py-16 text-slate-800">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-32 left-1/2 h-72 w-72 -translate-x-1/2 rounded-full bg-indigo-200/40 blur-[120px]" />
        <div className="absolute bottom-[-120px] left-12 h-80 w-80 rounded-full bg-blue-200/35 blur-[110px]" />
        <div className="absolute bottom-[-140px] right-[-120px] h-[420px] w-[420px] rounded-full bg-purple-100/60 blur-[120px]" />
        <div className="absolute right-[18%] top-[12%] h-56 w-56 rounded-full bg-amber-100/40 blur-[100px]" />
        <div className="absolute left-[8%] top-[30%] h-32 w-32 animate-[pulse-glow_6s_ease-in-out_infinite] rounded-full bg-white/30 blur-[80px]" />
      </div>

      <div className={cn('mx-auto grid w-full max-w-6xl gap-12 lg:grid-cols-[minmax(0,460px)_minmax(0,1fr)]', className)}>
        <div className="flex flex-col justify-center space-y-6 text-left">
          {eyebrow && (
            <span className="inline-flex w-fit animate-[fade-up_0.6s_ease-out_forwards] items-center rounded-full border border-slate-200 bg-white/90 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-indigo-500 shadow">
              {eyebrow}
            </span>
          )}
          {title && (
            <h1 className="animate-fade-rise text-4xl font-bold tracking-tight text-slate-900 md:text-5xl">
              {title}
            </h1>
          )}
          {description && (
            <p className="max-w-xl animate-[fade-up_0.8s_ease-out_forwards] text-base leading-relaxed text-slate-500">
              {description}
            </p>
          )}
          {side}
        </div>

        <div className="relative">
          <div className="absolute inset-0 -z-10 rounded-[32px] bg-white opacity-70 blur-3xl" aria-hidden />
          <div className="animate-[fade-in_0.6s_ease-out_forwards] rounded-[32px] border border-slate-200 bg-white p-10 shadow-[0_20px_60px_-30px_rgba(15,23,42,0.3)]">
            {children}
            {footer && <div className="mt-8 text-sm text-slate-500">{footer}</div>}
          </div>
        </div>
      </div>
    </div>
  )
}

export default FormLayout
