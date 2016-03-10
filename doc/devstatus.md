# Development status
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
        <td>Init(opts)</td>
        <td>Called when erocci start, <a href="https://github.com/occiware/erocci-dbus-java/issues/3">feature #3</a></td>
        <td>Done</td>
    </tr>
    <tr>
        <td>Terminate()</td>
        <td>Exit the application, <a href="https://github.com/occiware/erocci-dbus-java/issues/4">feature #4</a></td>
        <td>Done</td>
    </tr>
    <tr>
        <td>SaveResource(id, ...)</td>
        <td>Save a resource and add it to Configuration model, <a href="https://github.com/occiware/erocci-dbus-java/issues/5">feature #5</a></td>
        <td>Done</td>
    </tr> 
    <tr>
        <td>SaveLink(id, ...)</td>
        <td>Save a link and add it to Configuration model, <a href="https://github.com/occiware/erocci-dbus-java/issues/6">feature #6</a></td>
        <td>Done</td>
    </tr>
        <td>Update(id, ...)</td>
        <td>Update attributes of an entity, <a href="https://github.com/occiware/erocci-dbus-java/issues/7">feature #7</a></td>
        <td>Done</td>
    <tr>
    </tr>
        <td>SaveMixin(id, entities)</td>
        <td>Associate a list of entities with a mixin, replacing existing list if any previously given, <a href="https://github.com/occiware/erocci-dbus-java/issues/8">feature #8</a></td>
        <td>Problem</td>
    </tr>
    <tr>
        <td>UpdateMixin(id, entities)</td>
        <td>Associate a list of entities with a mixin, replacing existing list if any previously given, <a href="https://github.com/occiware/erocci-dbus-java/issues/9">feature #9</a></td>
        <td>Problem</td>
    </tr>
    <tr>
        <td>Find(id)</td>
        <td>Find an entity by his relative path, <a href="https://github.com/occiware/erocci-dbus-java/issues/10">feature #10</a></td>
        <td>Done</td>
    </tr>
    <tr>
        <td>Load(id)</td>
        <td>Describe an entity contents, <a href="https://github.com/occiware/erocci-dbus-java/issues/11">feature #11</a></td>
        <td>Done</td>
    </tr>
    <tr>
        <td>List(id, filters)</td>
        <td>Get an iterator for a collection: then use Next() to iterate, <a href="https://github.com/occiware/erocci-dbus-java/issues/12">feature #12</a></td>
        <td>Done</td>
    </tr>
    <tr>
        <td>Next(opaque_id, start, items)</td>
        <td>Retrieve items for a collection, from start(int) with number items, <a href="https://github.com/occiware/erocci-dbus-java/issues/13">feature #13</a></td>
        <td>Done</td>
    </tr>
    <tr>
        <td>Delete(id)</td>
        <td>Remove entity or dissociate mixin from configuration, <a href="https://github.com/occiware/erocci-dbus-java/issues/14">feature #14</a></td>
        <td>Done</td>
    </tr>
    <tr>
        <td>property schema (core interface)</td>
        <td>Assign an OCCI extension xml, on load, for now, extension are loaded via ConfigurationManager , for an owner and a Configuration Object, <a href="https://github.com/occiware/erocci-dbus-java/issues/15">feature #15</a></td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>AddMixin(id, location, owner)</td>
        <td>Add a user mixin, act as a user tag, <a href="https://github.com/occiware/erocci-dbus-java/issues/16">feature #16</a></td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>DelMixin(id, location, owner)</td>
        <td>Delete a user mixin, <a href="https://github.com/occiware/erocci-dbus-java/issues/17">feature #17</a></td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>Action(id, action_id, attributes)</td>
        <td>Execute Action command for real interaction, action is call properly, while action has no connector, it use OCL validator but cant launch a command, <a href="https://github.com/occiware/erocci-dbus-java/issues/18">feature #18</a></td>
        <td>In progress</td>
    </tr>
    <tr>
        <td>Cloud connector</td>
        <td>Connector based on CloudDesigner implementation, for multi cloud purpose</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>Docker connector</td>
        <td>Connector based on CloudDesigner implementation, for Docker container purpose</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>Hypervisor connector</td>
        <td>Connector based on CloudDesigner implementation, for Hypervisor (like xen, vmware etc.) purpose</td>
        <td>TODO</td>
    </tr>
    <tr>
        <td>Simulator connector</td>
        <td>Connector based on CloudDesigner implementation, for Simulator purpose</td>
        <td>TODO</td>
    </tr>

</table>


