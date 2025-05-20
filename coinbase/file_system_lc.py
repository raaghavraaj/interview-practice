class FileSystem:
    def __init__(self):
        self.storage = {}
        pass

    def ls(self, path: str):
        if path == '/':
            return list(self.storage.keys())
        path_list = path.strip('/').split('/')
        curr_dir = self.storage
        for dir in path_list:
            curr_dir = curr_dir[dir]
        if isinstance(curr_dir, dict):
            return list(curr_dir.keys())
        else:
            return [path_list[-1]]

    def mkdir(self, path: str):
        path_list = path.strip('/').split('/')
        curr_dir = self.storage
        for dir in path_list:
            if dir not in curr_dir:
                curr_dir[dir] = {}
            curr_dir = curr_dir[dir]
        
    def addContentToFile(self, path: str, content: str):
        path_list = path.strip('/').split('/')
        curr_dir = self.storage
        for dir in path_list[:-1]:
            if dir not in curr_dir:
                curr_dir[dir] = {}
            curr_dir = curr_dir[dir]
        if path_list[-1] not in curr_dir:
            curr_dir[path_list[-1]] = content
        else:
            curr_dir[path_list[-1]] += content

    def readContentFromFile(self, path: str):
        path_list = path.strip('/').split('/')
        curr_dir = self.storage
        for dir in path_list:
            curr_dir = curr_dir[dir]
        return curr_dir

if __name__ == '__main__':
    fs = FileSystem()
    print(fs.ls('/'))
    fs.mkdir("/a/b/c")
    print(fs.ls('/a/b/c'))
    fs.addContentToFile('/a/b/c/d', 'hello')
    print(fs.readContentFromFile('/a/b/c/d'))