//
//  Logger+Finances.swift
//  FinanceKit
//
//  Created by Matthias Hochgatterer on 03.04.18.
//  Copyright © 2018 Matthias Hochgatterer. All rights reserved.
//
import Foundation

fileprivate let _fileManager = FileManager()
public var LogDirectoryName = "Logs"

public struct Logging {
    /// The list is sorted ascending by file creation date.
    ///
    /// - Returns: A list of file urls containing application logs.
    public static var fileURLs: [URL]?  {
        guard let logsDir = defaultLogsDirectoryURL()  else {
            return nil
        }
        if let urls = try? _fileManager.contentsOfDirectory(at: logsDir, includingPropertiesForKeys: [.creationDateKey], options: .skipsHiddenFiles) {
            return urls.sorted(by: {
                lhs, rhs in
                
                guard let lhsDate = (try? lhs.resourceValues(forKeys: [.creationDateKey]))?.creationDate, let rhsDate = (try? rhs.resourceValues(forKeys: [.creationDateKey]))?.creationDate else {
                    return true
                }
                
                return lhsDate > rhsDate
            })
        }
        
        return nil
    }
    
    public static func defaultLogsDirectoryURL() -> URL? {
        // Check user preference for directory type
        let useCachesDirectory = UserDefaults.standard.bool(forKey: "FlutterLogs_UseCachesDirectory")
        let searchPathDirectory: FileManager.SearchPathDirectory = useCachesDirectory ? .cachesDirectory : .applicationSupportDirectory

        do {
            let dir = try _fileManager.url(for: searchPathDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
            var dirURL = dir.appendingPathComponent(LogDirectoryName)

            Swift.print("FlutterLogs: Final log directory path: \(dirURL.path)")

            // Create directory if needed
            if !_fileManager.fileExists(atPath: dirURL.path) {
                try _fileManager.createDirectory(at: dirURL, withIntermediateDirectories: true, attributes: nil)
                Swift.print("FlutterLogs: Created directory: \(dirURL.path)")

                // Exclude Logs directory from backups
                var values = URLResourceValues()
                values.isExcludedFromBackup = true
                try dirURL.setResourceValues(values)
            } else {
                Swift.print("FlutterLogs: Directory already exists: \(dirURL.path)")
            }
            return dirURL
        } catch let error as NSError {
            Swift.print("Could not find Application Support directory:", error.localizedDescription)
        }

        return nil
    }
}
