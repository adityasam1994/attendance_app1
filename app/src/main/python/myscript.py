import face_recognition, os
from os.path import dirname, join
import pickle

def get_base_dir():
    di = dirname(__file__)
    nm = []
    for filename in os.listdir(di):
        if filename.endswith(".pkl"):
            nm.append(filename)

    return nm

def main(iname):
    all_faces = get_base_dir()

    faces = []
    for face in all_faces:
        faces.append(join(dirname(__file__), face))

    #known_image = face_recognition.load_image_file(robert)
    unknown_image = face_recognition.load_image_file(iname)

    #robert_encoding = face_recognition.face_encodings(known_image)[0]

    all_encodings = []

    for face in faces:
        with open(face, "rb") as f:
            all_encodings.append(pickle.load(f))

    unknown_encoding = face_recognition.face_encodings(unknown_image)
    if len(unknown_encoding) > 0:
        unknown_encoding = unknown_encoding[0]
        results = face_recognition.compare_faces(all_encodings, unknown_encoding)
        res = ""
        for result in results:
            if result == True:
                res = results.index(result)

        if res != "":
            nm = str(all_faces[res]).replace(".pkl", "")
        else:
            nm = "Cannot recognize"


        return nm
    else:
        return "No Face Found"
    #return str(os.environ["HOME"])