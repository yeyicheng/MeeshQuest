import sys
from xml.dom import minidom

# ***NOTE***: input file should have no extra blank lines!

_IMPL = minidom.getDOMImplementation()
_ROOT_ATTRS = {
        "xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
        "xsi:noNamespaceSchemaLocation": "part2in.xsd"
        }

class Commands(object):
    def do_city(self, doc, name, x, y):
        me = doc.createElement("createCity")
        me.setAttribute("name", name)
        me.setAttribute("x", x)
        me.setAttribute("y", y)
        me.setAttribute("radius", "5")
        me.setAttribute("color", "black")
        return me
        
    def do_mapCity(self, doc, name):
        me = doc.createElement("mapCity")
        me.setAttribute("name", name)
        return me
        
    def do_printTree(self, doc, id_num):
        me = doc.createElement("printPMQuadtree")
        me.setAttribute("id", id_num)
        return me
    
    def do_road(self, doc, start, end):
        me = doc.createElement("mapRoad")
        me.setAttribute("start", start)
        me.setAttribute("end", end)
        return me

    def do_savemap(self, doc, name):
        me = doc.createElement("saveMap")
        me.setAttribute("name", name)
        return me

    def do_delete(self, doc, name):
        me = doc.createElement("deleteCity")
        me.setAttribute("name", name)
        return me
    
    def do_rangeRoads(self, doc, x, y, radius):
        me = doc.createElement("rangeRoads")
        me.setAttribute("x", x)
        me.setAttribute("y", y)                    
        me.setAttribute("radius", radius)
        return me
    
    def do_nearestRoad(self, doc, x, y):
        me = doc.createElement("nearestRoad")
        me.setAttribute("x", x)
        me.setAttribute("y", y)                    
        return me    
          
    def do_nearestCity(self, doc, x, y):
        me = doc.createElement("nearestCity")
        me.setAttribute("x", x)
        me.setAttribute("y", y)                    
        return me  
    
    def do_nearestIsolatedCity(self, doc, x, y):
        me = doc.createElement("nearestIsolatedCity")
        me.setAttribute("x", x)
        me.setAttribute("y", y)                    
        return me    
          
def main(f):
    doc = _IMPL.createDocument(None, "commands", None)
    id = 1
    root = doc.documentElement
    for (name, attr) in _ROOT_ATTRS.items():
        root.setAttribute(name, attr)
    cmd_attrs = f.readline().split()
    # first line should be WIDTH HEIGHT PMORDER LEAFORDER
    # space-separated, in that order
    root.setAttribute("spatialWidth", cmd_attrs[0])
    root.setAttribute("spatialHeight", cmd_attrs[1])
    root.setAttribute("pmOrder", cmd_attrs[2])
    root.setAttribute("g", cmd_attrs[3])
    c = Commands()
    for line in f:
        args = line.split()
        cmd = args.pop(0)
        fname = "do_" + cmd
        if hasattr(c, fname):
            fn = getattr(c, fname)
            elt = fn(doc, *args)
        else:
            elt = doc.createElement(cmd)
        elt.setAttribute("id", str(id))
        id += 1
        root.appendChild(elt)
    print(doc.toprettyxml())

if __name__ == '__main__':
    with open(sys.argv[1], "r") as f:
        main(f)
