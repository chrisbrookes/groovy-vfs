/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2017
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */
// ============================================================================
// (C) Copyright Schalk W. Cronje 2013
//
// This software is licensed under the Apache License 2.0
// See http://www.apache.org/licenses/LICENSE-2.0 for license details
//
// Unless required by applicable law or agreed to in writing, software distributed under the License is
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and limitations under the License.
//
// ============================================================================

package org.ysb33r.groovy.dsl.vfs


import spock.lang.*
import org.ysb33r.groovy.dsl.vfs.helpers.*
import static org.ysb33r.groovy.dsl.vfs.helpers.ListFolderTestHelper.*
import org.ysb33r.groovy.dsl.vfs.FileActionException

abstract class SchemaSpec extends Specification {
    @Shared def server
    
   def vfs
 
   def setup() {
       vfs=VFSBuilder.build(this.class.name)
       server.TESTFSWRITEROOT.mkdirs()
   }
   
   def cleanup()  {
       assert server.TESTFSWRITEROOT.deleteDir()
   }
   
   def "Can we list files on server "() {
       expect:
           assertListable vfs, server.READROOT
   }
 
   def "Copy from remote site, overwriting an existing file" () {
       setup:
         vfs.cp "${server.READROOT}/file1.txt", server.TESTFSWRITEROOT, overwrite:false
         vfs.cp "${server.READROOT}/file2.txt", new File("${server.TESTFSWRITEROOT}/file1.txt"), overwrite:true
         
         
       expect:
         new File ("${server.TESTFSWRITEROOT}/file1.txt").text == new File ("${server.TESTFSREADONLYROOT}/file2.txt").text
   }
   
   def "Copy an archive from a remote site and explode it locally" () {
       setup:
         vfs.cp "tbz2:${server.ARCHIVEROOT}/test-files.tar.bz2", server.TESTFSWRITEROOT, overwrite:false, recursive:true
         
       expect:
         new File( "${server.TESTFSREADONLYROOT}/${filename}" ).exists()
         
       where:
         filename                | _
         'file1.txt'             | _
         'file2.txt'             | _
         'test-subdir/file3.txt' | _  
         'test-subdir/file4.txt' | _   
   }    

   def "Copy an archive from an remote site twice and explode it locally, but don't overwrite" () {
       setup:
         vfs.cp "tbz2:${server.ARCHIVEROOT}/test-files.tar.bz2", server.TESTFSWRITEROOT, overwrite:false, recursive:true
         
       when:
         vfs.cp "tbz2:${server.ARCHIVEROOT}/test-files.tar.bz2", server.TESTFSWRITEROOT, overwrite:false, recursive:true
         
       then:
         thrown (FileActionException)  
   }    

   def "Copy an archive from a remote site twice and explode it locally, overwrite locally" () {
       setup:
         vfs.cp "tbz2:${server.ARCHIVEROOT}/test-files.tar.bz2", server.TESTFSWRITEROOT, overwrite:false, recursive:true
         vfs.cp "tbz2:${server.ARCHIVEROOT}/test-files.tar.bz2", server.TESTFSWRITEROOT, overwrite:true,  recursive:true
         
       expect:
         new File( "${server.TESTFSREADONLYROOT}/${filename}" ).exists()
         
       where:
         filename                | _
         'file1.txt'             | _
         'file2.txt'             | _
         'test-subdir/file3.txt' | _  
         'test-subdir/file4.txt' | _   
   }

    def "Can we copy a directory recursively"() {

        when:
            vfs.cp server.READROOT, "${server.WRITEROOT}", overwrite:true, recursive:true

        then:
            new File(server.TESTFSWRITEROOT,'file1.txt').exists()
            new File(server.TESTFSWRITEROOT,'file2.txt').exists()
            new File(server.TESTFSWRITEROOT,'test-subdir/file3.txt').exists()
            new File(server.TESTFSWRITEROOT,'test-subdir/file4.txt').exists()
    }

   /*  
   def "Copy from remote site, using apache commons" () {
       setup: "This test was put in place to verify that a bug existed in groovy-vfs, but not in Apache VFS"
         def f1 = vfs.fsMgr.resolveFile "${server.READROOT}/file1.txt"
         def f2 = vfs.fsMgr.resolveFile "${server.READROOT}/file2.txt"
         def target = vfs.fsMgr.toFileObject new File("${server.TESTFSWRITEROOT}/file1.txt")
         
         target.copyFrom(f1,org.apache.commons.vfs2.Selectors.SELECT_ALL)
         target.copyFrom(f2,org.apache.commons.vfs2.Selectors.SELECT_ALL)
         
       expect:
         new File ("${server.TESTFSWRITEROOT}/file1.txt").text == new File ("${server.TESTFSREADONLYROOT}/file2.txt").text
   }
   */
}
