#!/usr/bin/env python3
"""
Redraws the Play Store icon and feature graphic.

    python3 scripts/store-graphics.py

Neither asset exists as a raster anywhere in the project - the launcher icon is an
adaptive vector - so they are generated from the same shape and palette the app uses
rather than hand-drawn, and stay in step with it if the artwork changes.

Pure standard library: no Pillow, no ImageMagick, nothing to install.
"""
import math
import os
import pathlib
import struct
import zlib

os.chdir(pathlib.Path(__file__).resolve().parent.parent)

BG_TOP = (0x12, 0x20, 0x2C)
BG_BOTTOM = (0x17, 0x2B, 0x3A)
GOLD = (0xE8, 0xA5, 0x3A)
INK = (0xE9, 0xEE, 0xF2)


def write_png(path,w,h,px):
    raw=b"".join(b"\x00"+bytes(px[y*w*3:(y+1)*w*3]) for y in range(h))
    def chunk(t,d): return struct.pack(">I",len(d))+t+d+struct.pack(">I",zlib.crc32(t+d)&0xffffffff)
    pathlib.Path(path).write_bytes(b"\x89PNG\r\n\x1a\n"+chunk(b"IHDR",struct.pack(">IIBBBBB",w,h,8,2,0,0,0))+chunk(b"IDAT",zlib.compress(raw,9))+chunk(b"IEND",b""))
def write_png(path,w,h,px):
    raw=b"".join(b"\x00"+bytes(px[y*w*3:(y+1)*w*3]) for y in range(h))
    def chunk(t,d): return struct.pack(">I",len(d))+t+d+struct.pack(">I",zlib.crc32(t+d)&0xffffffff)
    pathlib.Path(path).write_bytes(b"\x89PNG\r\n\x1a\n"+chunk(b"IHDR",struct.pack(">IIBBBBB",w,h,8,2,0,0,0))+chunk(b"IDAT",zlib.compress(raw,9))+chunk(b"IEND",b""))
class Canvas:
    def __init__(s,w,h): s.w,s.h,s.px=w,h,bytearray(w*h*3)
    def fill_gradient(s,top,bottom,split=0.5):
        for y in range(s.h):
            c=top if y/max(1,s.h-1)<split else bottom
            for x in range(s.w):
                i=(y*s.w+x)*3; s.px[i:i+3]=bytes(c)
    def blend(s,x,y,c,a):
        if not(0<=x<s.w and 0<=y<s.h) or a<=0: return
        a=min(1.0,a); i=(y*s.w+x)*3
        for k in range(3): s.px[i+k]=int(s.px[i+k]*(1-a)+c[k]*a)
    def poly(s,pts,c,ss=3):
        xs=[p[0] for p in pts]; ys=[p[1] for p in pts]
        for y in range(max(0,int(min(ys))),min(s.h,int(max(ys))+2)):
            for x in range(max(0,int(min(xs))),min(s.w,int(max(xs))+2)):
                hits=0
                for sy in range(ss):
                    for sx in range(ss):
                        px_,py_=x+(sx+0.5)/ss,y+(sy+0.5)/ss
                        inside=False; j=len(pts)-1
                        for i2 in range(len(pts)):
                            xi,yi=pts[i2]; xj,yj=pts[j]
                            if (yi>py_)!=(yj>py_) and px_<(xj-xi)*(py_-yi)/(yj-yi+1e-9)+xi: inside=not inside
                            j=i2
                        hits+=inside
                if hits: s.blend(x,y,c,hits/(ss*ss))
    def disc(s,cx,cy,r,c,ss=3):
        for y in range(max(0,int(cy-r-1)),min(s.h,int(cy+r+2))):
            for x in range(max(0,int(cx-r-1)),min(s.w,int(cx+r+2))):
                hits=0
                for sy in range(ss):
                    for sx in range(ss):
                        dx=x+(sx+0.5)/ss-cx; dy=y+(sy+0.5)/ss-cy
                        hits+=dx*dx+dy*dy<=r*r
                if hits: s.blend(x,y,c,hits/(ss*ss))
    def rect(s,x0,y0,x1,y1,c): s.poly([(x0,y0),(x1,y0),(x1,y1),(x0,y1)],c)
def tank(c,ox,oy,s,colour):
    def P(x,y): return (ox+x*s, oy+y*s)
    c.poly([P(4,16),P(6,16),P(7,13),P(17,13),P(18,16),P(20,16),P(18,10),P(15,6),P(9,6),P(6,10)],colour)
    c.disc(ox+7*s,oy+18.6*s,1.6*s,colour); c.disc(ox+17*s,oy+18.6*s,1.6*s,colour)
    c.rect(ox+13*s,oy+4*s,ox+19*s,oy+6*s,colour)
STROKES={"W":[(0,0,1,7),(1,7,2,3),(2,3,3,7),(3,7,4,0)],"A":[(0,7,2,0),(2,0,4,7),(0.9,4.4,3.1,4.4)],
 "R":[(0,7,0,0),(0,0,3,0),(3,0,3,3.4),(3,3.4,0,3.4),(0,3.4,3.4,7)],"F":[(0,7,0,0),(0,0,3.2,0),(0,3.4,2.6,3.4)],
 "O":[(0,0,3,0),(3,0,3,7),(3,7,0,7),(0,7,0,0)],"G":[(3,0,0,0),(0,0,0,7),(0,7,3,7),(3,7,3,4),(3,4,1.7,4)],
 "E":[(0,7,0,0),(0,0,3.2,0),(0,3.4,2.6,3.4),(0,7,3.2,7)]}
def word(c,text,x,y,unit,weight,colour):
    for ch in text:
        for (x0,y0,x1,y1) in STROKES[ch]:
            ax,ay=x+x0*unit,y+y0*unit; bx,by=x+x1*unit,y+y1*unit
            dx,dy=bx-ax,by-ay; n=max(2,int(math.hypot(dx,dy)))
            for i in range(n+1): c.disc(ax+dx*i/n,ay+dy*i/n,weight,colour,ss=2)
        x+=unit*4.6
    return x

def build():
    # 512 x 512 icon: the launcher tank, centred, on the app's own background.
    icon = Canvas(512, 512)
    icon.fill_gradient(BG_TOP, BG_BOTTOM)
    tank(icon, 512 * 0.5 - 12 * 17.5, 512 * 0.5 - 12.5 * 17.5, 17.5, GOLD)
    write_png("playstore/icon-512.png", 512, 512, icon.px)
    print("playstore/icon-512.png")

    # 1024 x 500 feature graphic: wordmark left, tank right, horizon between them.
    fg = Canvas(1024, 500)
    fg.fill_gradient(BG_TOP, BG_BOTTOM, split=0.62)
    fg.rect(0, 308, 1024, 311, (0x1E, 0x35, 0x46))
    tank(fg, 705, 140, 13.5, GOLD)
    word(fg, "WARFORGE", 62, 168, 13.0, 5.2, INK)
    fg.rect(64, 276, 560, 279, GOLD)
    write_png("playstore/feature-graphic-1024x500.png", 1024, 500, fg.px)
    print("playstore/feature-graphic-1024x500.png")


if __name__ == "__main__":
    build()
