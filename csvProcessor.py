import csv
import sys

ctr = 0
running = []
walking = []
eating = []
try:
    f = open("data.csv", 'rt')
    reader = csv.reader(f)
    bool = True
    for row in reader:
        if bool:
            bool = False
            continue
        data = row
        i=1
        if data[len(data)-1] is '0':
            while i<len(data)-3:
                running.append(data[i] + "," + data[i+1] + "," + data[i+2])
                i+=3
        elif data[len(data)-1] is '1':
            while i < len(data) - 3:
                walking.append(data[i] + "," + data[i+1] + "," + data[i+2])
                i += 3
        else:
            while i < len(data) - 3:
                eating.append(data[i] + "," + data[i+1] + "," + data[i+2])
                i += 3
    f = open("visualdatatest.csv", 'wt')
    writer = csv.writer(f)
    writer.writerow(('x1','y1','z1','x2','y2','z2','x3','y3','z3'))
    print len(running)
    print len(walking)
    print len(eating)
    for a,b,c in zip(running,walking,eating):
        writer.writerow([a,b,c])

finally:
    f.close()
