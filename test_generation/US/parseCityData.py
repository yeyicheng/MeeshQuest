
f = open("UScitytable.txt", "r")

text = f.read()
text = text.trim()

a = text.split("</tr>")

print a
