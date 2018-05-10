import random

with open('moves1.txt', 'a') as myfile:
  for i in range(200000):
    myfile.write(str(random.randint(0, 6)) + '\n')
