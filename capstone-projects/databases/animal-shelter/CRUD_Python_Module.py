# Example Python Code to Insert a Document

from pymongo import MongoClient
from bson.objectid import ObjectId

class AnimalShelter(object):
    """ CRUD operations for Animal collection in MongoDB """

    def __init__(self, username, password):
        # Initializing the MongoClient. This helps to access the MongoDB
        # databases and collections. This is hard-wired to use the aac
        # database and the animals collection.
        #
        # The username and password are passed in from the dashboard.
        #
        # Connection Variables
        #
        USER = username
        PASS = password
        HOST = 'localhost'
        PORT = 27017
        DB = 'aac'
        COL = 'animals'
        #
        # Initialize Connection
        #
        self.client = MongoClient('mongodb://%s:%s@%s:%d' % (USER,PASS,HOST,PORT))
        self.database = self.client['%s' % (DB)]
        self.collection = self.database['%s' % (COL)]

    # Create a method to return the next available record number for use in the create method
    
    # Complete this create method to implement the C in CRUD.
    def create(self, data):
        """Insert a document into the animals collection."""
        if data is not None:
            try:
                result = self.collection.insert_one(data)
                return result.acknowledged
            except Exception as e:
                print("An error occurred while inserting data:", e)
                return False
        else:
            return False

    # Create method to implement the R in CRUD.
    def read(self, query):
        """Query documents from the animals collection."""
        if query is not None:
            try:
                result = self.collection.find(query)
                return list(result)
            except Exception as e:
                print("An error occurred while reading data:", e)
                return []
        else:
            return []
        
    # Create method to implement the U in CRUD.
    def update(self, query, new_values):
        """Update document(s) in the animals collection."""
        if query is not None and new_values is not None:
            try:
                result = self.collection.update_many(
                    query,
                    {"$set": new_values}
                )
                return result.modified_count
            except Exception as e:
                print("An error occurred while updating data:", e)
                return 0
        else:
            return 0

    # Create method to implement the D in CRUD.
    def delete(self, query):
        """Delete document(s) from the animals collection."""
        if query is not None:
            try:
                result = self.collection.delete_many(query)
                return result.deleted_count
            except Exception as e:
                print("An error occurred while deleting data:", e)
                return 0
        else:
            return 0