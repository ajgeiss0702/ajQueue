/*

This is a typescript file for a few reasons:

    (a). Makes it easier to handle errors when uploading.
        (i). I am not comfortable enough with bash scripting to handle errors properly.
    (b). Allows for cleaner code. Before I made this, the upload was a single line in the workflow yml file, which was very ugly and very annoying to edit.
    (c). Typescript is the only scripting language I am competent at enough to do this properly.
    (d). Not javascript because typescript adds type safety and a few utils, which is nice.


Here are the old commands for reference:
          curl --no-progress-meter -A "AJUPDATER/1.0" -H "Authorization: $MODRINTH_TOKEN" -F data="{\"project_id\": \"dzacATni\", \"version_number\": \"$VERSION\", \"name\": \"Pre-release v$VERSION\", \"changelog\": \"Note: This is a (most likely) un-tested build. It is not guarenteed to work.<br><br>Change since previous build:<br><a href=\\\"${{ github.event.compare }}\\\" target=\\\"_blank\\\">${{ github.event.head_commit.message }}</a>\", \"file_parts\": [\"file\"], \"version_type\": \"beta\", \"loaders\": [\"bungeecord\", \"velocity\"], \"featured\": false, \"game_versions\": $(curl https://ajg0702.us/pl/updater/mc-versions.php), \"dependencies\": [], \"primary_file\": \"file\"}" -F "file=@free/build/libs/ajQueue-$VERSION.jar" "https://api.modrinth.com/v2/version"
          curl -F "file=@free/build/libs/ajQueue-$VERSION.jar" -F api_key=$POLYMART_TOKEN -F resource_id="2535" -F version="$VERSION" -F title="Pre-release v$VERSION" -F beta=1 -F message=$'Note: This is a (most likely) un-tested build. It is not guarenteed to work!\n\nChange since previous build:\n[url=${{ github.event.compare }}"]${{ github.event.head_commit.message }}[/url]' "https://api.polymart.org/v1/postUpdate"
          curl -F "file=@premium/build/libs/ajQueuePlus-$VERSION.jar" -F api_key=$POLYMART_TOKEN -F resource_id="2714" -F version="$VERSION" -F title="Pre-release v$VERSION" -F beta=1 -F message=$'Note: This is a (most likely) un-tested build. It is not guarenteed to work!\n\nChange since previous build:\n[url=${{ github.event.compare }}"]${{ github.event.head_commit.message }}[/url]' "https://api.polymart.org/v1/postUpdate"

 */

// https://docs.github.com/en/actions/writing-workflows/choosing-what-your-workflow-does/accessing-contextual-information-about-workflow-runs#github-context
import * as fs from 'fs/promises';

const github: {
    event: GithubPushEvent
} | null = JSON.parse(process.env.GH ?? "null");

const MODRINTH_TOKEN = process.env.MODRINTH_TOKEN;
const POLYMART_TOKEN = process.env.POLYMART_TOKEN;

if(!MODRINTH_TOKEN) {
    console.error("Missing MODRINTH_TOKEN!");
    process.exit(1);
}
if(!POLYMART_TOKEN) {
    console.error("Missing POLYMART_TOKEN!");
    process.exit(1);
}

if(!github?.event) {
    console.error("Missing Github event!");
    process.exit(1);
}


(async () => {

    console.log("Fetching mc versions, changes, version, and file...")

    const versions = await fetch("https://ajg0702.us/pl/updater/mc-versions.php")
        .then(res => res.json());

    const changes = github.event.commits;

    const version = await fs.readFile("build.gradle.kts", "utf8")
        .then(f => f.split("version = \"")[1].split('"')[0]);

    const file = new File(
        [new Uint8Array([...await fs.readFile(`free/build/libs/ajQueue-${version}.jar`).then(r => r.toJSON().data)])],
        `ajQueue-${version}.jar`
    );
    const plusFile = new File(
        [new Uint8Array([...await fs.readFile(`premium/build/libs/ajQueuePlus-${version}.jar`).then(r => r.toJSON().data)])],
        `ajQueuePlus-${version}.jar`
    );




    console.log("Uploading to Modrinth...");

    const modrinthData = new FormData();

    modrinthData.set("data", JSON.stringify({
        "project_id": "dzacATni",
        "version_number": `${version}`,
        "name": `Pre-release v${version}`,
        "changelog":
`Note: This is a potentially unstable (and possibly untested) build. It is not guaranteed to work, and may have issues.<br>
If you do decide to run this, make sure to report any issues to support.<br>
<br>
Changes in this build:<br>
${changes.map(c => `<a href="${c.url}">${c.message}</a><br>`).join("")}

${changes.length > 1 ? `<br><a href="${github.event.compare}">View combined changes</a>` : ``}
`,
        "file_parts": ["file"],
        "version_type": "beta",
        "loaders": ["bungeecord","velocity"],
        "featured": false,
        "game_versions": versions,
        "dependencies": [],
        "primary_file": "file"
    }));
    modrinthData.set("file", file)

    const modrinthResponse = await fetch("https://api.modrinth.com/v2/version", {
        method: "POST",
        headers: {
            "User-Agent": "ajUpdater/2.0",
            "Authorization": MODRINTH_TOKEN
        },
        body: modrinthData
    });

    if(!modrinthResponse.ok) {
        console.warn("Modrinth response failed.", await modrinthResponse.text());
    } else {
        console.info("Modrinth succeeded.", await modrinthResponse.text())
    }






    console.log("Uploading ajQueuePlus to Polymart...");

    const polymartPlusResponse = await uploadToPolymart(github.event, "2714", version, changes, plusFile);

    if(!polymartPlusResponse.ok) {
        console.warn("Polymart plus response failed.", await polymartPlusResponse.text());
    } else {
        console.warn("Polymart plus succeeded.", await polymartPlusResponse.text());
    }

    console.log("Uploading ajQueue to Polymart...");

    const polymartFreeResponse = await uploadToPolymart(github.event, "2535", version, changes, file);

    if(!polymartFreeResponse.ok) {
        console.warn("Polymart free response failed.", await polymartFreeResponse.text());
    } else {
        console.warn("Polymart free succeeded.", await polymartFreeResponse.text());
    }

})();

async function uploadToPolymart(event: GithubPushEvent, resource_id: string, version: string, changes: GithubCommit[], file: File) {
    if(!POLYMART_TOKEN) {
        console.error("Missing POLYMART_TOKEN!");
        process.exit(1);
    }
    const polymartData = new FormData();
    polymartData.set("api_key", POLYMART_TOKEN);
    polymartData.set("resource_id", resource_id);
    polymartData.set("version", version);
    polymartData.set("title", `Pre-release v${version}`);
    polymartData.set("beta", "1");
    polymartData.set("message",
        `Note: This is a potentially unstable (and possibly untested) build. It is not guaranteed to work, and may have issues.
If you do decide to run this, make sure to report any issues to support.

Changes in this build:
${changes.map(c => `[url=${c.url}]${c.message}[/url]` + "\n").join()}

${changes.length > 1 ? "\n" + `[url=${event.compare}]View combined changes[/url]` : ``}
`);
    polymartData.set("file", file);

    return await fetch("https://api.polymart.org/v1/postUpdate", {
        method: "POST",
        headers: {
            "User-Agent": "ajUpdater/2.0",
        },
        body: polymartData
    });
}




// https://docs.github.com/en/webhooks/webhook-events-and-payloads#push
type GithubPushEvent = {
    after: string,
    base_ref: string | null,
    before: string,
    commits: GithubCommit[],
    compare: string,
    created: boolean,
    deleted: boolean,
    forced: boolean,
    head_commit: GithubCommit,
    pusher: {
        date: string,
        email: string | null,
        name: string,
        username: string
    },
    ref: string,
    repository: unknown,
    sender: unknown
}

type GithubCommit = {
    added: string[],
    author: {
        date: string,
        email: string | null,
        name: string,
        username: string
    },
    committer: {
        date: string,
        email: string | null,
        name: string,
        username: string
    },
    distinct: boolean,
    id: string,
    message: string,
    modified: string[],
    removed: string[],
    timestamp: string,
    tree_id: string,
    url: string
}