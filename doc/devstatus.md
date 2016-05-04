# Development status
[![Build Status](https://travis-ci.org/occiware/erocci-dbus-java.svg?branch=master)](https://travis-ci.org/occiware/erocci-dbus-java)

Development Status values :
* TODO (Nothing has been done but will be done in near future)
* In progress (in devlopment progress)
* Done (works and tested)
* Problem (done but not tested or have bugs)

## Tasks
<table>
    <th>task name</th>
    <th>comment (and related issues)</th>
    <th>progress status</th>
    
    <tr>
        <td align="center">Init(opts)</td>
        <td align="center">Called when erocci start, <a href="https://github.com/occiware/erocci-dbus-java/issues/3">feature #3</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Terminate()</td>
        <td align="center">Exit the application, <a href="https://github.com/occiware/erocci-dbus-java/issues/4">feature #4</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">SaveResource(id, ...)</td>
        <td align="center">Save a resource and add it to Configuration model, <a href="https://github.com/occiware/erocci-dbus-java/issues/5">feature #5</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr> 
    <tr>
        <td align="center">SaveLink(id, ...)</td>
        <td align="center">Save a link and add it to Configuration model, <a href="https://github.com/occiware/erocci-dbus-java/issues/6">feature #6</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
        <td align="center">Update(id, ...)</td>
        <td align="center">Update attributes of an entity, <a href="https://github.com/occiware/erocci-dbus-java/issues/7">feature #7</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    <tr>
    </tr>
        <td align="center">SaveMixin(id, entities)</td>
        <td align="center">Associate a list of entities with a mixin, replacing existing list if any previously given, <a href="https://github.com/occiware/erocci-dbus-java/issues/8">feature #8</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">UpdateMixin(id, entities)</td>
        <td align="center">Associate a list of entities with a mixin, replacing existing list if any previously given, <a href="https://github.com/occiware/erocci-dbus-java/issues/9">feature #9</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Find(id)</td>
        <td align="center">Find an entity by his relative path, <a href="https://github.com/occiware/erocci-dbus-java/issues/10">feature #10</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Load(id)</td>
        <td align="center">Describe an entity contents, <a href="https://github.com/occiware/erocci-dbus-java/issues/11">feature #11</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">List(id, filters)</td>
        <td align="center">Get an iterator for a collection: then use Next() to iterate, <a href="https://github.com/occiware/erocci-dbus-java/issues/12">feature #12</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Next(opaque_id, start, items)</td>
        <td align="center">Retrieve items for a collection, from start(int) with number items, <a href="https://github.com/occiware/erocci-dbus-java/issues/13">feature #13</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Delete(id)</td>
        <td align="center">Remove entity or dissociate mixin from configuration, <a href="https://github.com/occiware/erocci-dbus-java/issues/14">feature #14</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">property schema (core interface)</td>
        <td align="center">Assign an OCCI extension xml, on load, for now, extension are loaded via ConfigurationManager , for an owner and a Configuration Object, <a href="https://github.com/occiware/erocci-dbus-java/issues/15">feature #15</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">AddMixin(id, location, owner)</td>
        <td align="center">Add a user mixin, act as a user tag, <a href="https://github.com/occiware/erocci-dbus-java/issues/16">feature #16</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">DelMixin(id, location, owner)</td>
        <td align="center">Delete a user mixin, <a href="https://github.com/occiware/erocci-dbus-java/issues/17">feature #17</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Action(id, action_id, attributes)</td>
        <td align="center">Execute Action command for real interaction, action is call properly, while action has no connector, it use OCL validator, <a href="https://github.com/occiware/erocci-dbus-java/issues/18">feature #18</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Dummy connector</td>
        <td align="center">Connector attach to default infrastructure model, <a href="https://github.com/occiware/erocci-dbus-java/issues/21">feature #21</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Cloud connector</td>
        <td align="center">Connector based on CloudDesigner implementation, for multi cloud purpose, <a href="https://github.com/occiware/erocci-dbus-java/issues/27">feature #27</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/inprogress.png" alt="In progress" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Docker connector</td>
        <td align="center">Connector based on CloudDesigner implementation, for Docker container purpose, <a href="https://github.com/occiware/erocci-dbus-java/issues/22">feature #22</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Custom connector, light demo</td>
        <td align="center">Custom connector based on Clouddesigner model of a light demo, <a href="https://github.com/occiware/erocci-dbus-java/issues/25">feature #25</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/done.png" alt="Done" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Hypervisor connector</td>
        <td align="center">Connector based on CloudDesigner implementation, for VMware, <a href="https://github.com/occiware/erocci-dbus-java/issues/29">feature #29</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/inprogress.png" alt="In progress" height="40" width="auto" /></td>
    </tr>
    <tr>
        <td align="center">Refactoring ConfigurationManager</td>
        <td align="center">Refactoring on CloudDesigner model reversing implementation, <a href="https://github.com/occiware/erocci-dbus-java/issues/30">feature #30</a></td>
        <td align="center"><img src="https://raw.github.com/occiware/erocci-dbus-java/master/doc/inprogress.png" alt="In progress" height="40" width="auto" /></td>
    </tr>

</table>


