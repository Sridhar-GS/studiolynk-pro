# Portfolio and S3 Specification

## Portfolio
One freelancer -> one portfolio.

Portfolio:
- categories
- images
- image order

CRUD:
- create category
- edit category
- delete category
- upload image
- edit image metadata if used
- delete image
- reorder images

## AWS S3 Organized Folder Layout
All media assets are organized in an **entity-centric hierarchy** ensuring clean isolation, visual clarity in AWS S3 Console, and chronological sorting:

```
studiolynk-bucket-2026/
├── studios/
│   └── {studioId}/
│       ├── profile/
│       │   └── {timestamp}_{sanitized_original_filename}.{ext}
│       └── documents/
│           └── {timestamp}_{sanitized_original_filename}.{ext}
├── freelancers/
│   └── {freelancerId}/
│       ├── profile/
│       │   └── {timestamp}_{sanitized_original_filename}.{ext}
│       └── portfolio/
│           └── {categoryId}_{categorySlug}/
│               ├── {timestamp}_{sanitized_original_filename}.{ext}
│               └── ...
└── users/
    └── {userId}/
        └── profile/
            └── {timestamp}_{sanitized_original_filename}.{ext}
```

### Key Generation Rules
1. **Studio Profiles**: `studios/{studioId}/profile/{timestamp}_{sanitized_filename}.{ext}`
2. **Freelancer Profiles**: `freelancers/{freelancerId}/profile/{timestamp}_{sanitized_filename}.{ext}`
3. **Freelancer Portfolio Categories**: `freelancers/{freelancerId}/portfolio/{categoryId}_{categorySlug}/{timestamp}_{sanitized_filename}.{ext}`
   - `categorySlug`: lower-cased alphanumeric string with spaces converted to underscores (e.g. `12_wedding_photography`).
   - `timestamp`: epoch millis ensuring uniqueness and chronological ordering in AWS Console.
   - `sanitized_filename`: original base filename sanitized to alphanumeric, dashes, and underscores.

## Public Access URL
Objects resolve to standard public AWS S3 URLs:
`https://{bucketName}.s3.{region}.amazonaws.com/{s3Key}`

## Upload Architecture
Backend-mediated multipart upload with validation:
- MIME types restricted to `image/jpeg`, `image/png`, `image/webp`
- File size up to 10 MB per file
- Multi-image bulk upload to portfolio categories
- Direct S3 profile upload via `POST /api/upload/image` for studio logos and freelancer avatars

## Database Mapping
Store:
- object key (`s3_key`)
- public URL (`image_url`)
- original filename (`original_filename`)
- MIME type (`content_type`)
- size in bytes (`file_size`)
- sort order (`sort_order`)
- category relationship (`category_id`)

## UI
- Portfolio categories use responsive image grids with drag-and-drop reordering.
- Studio and Freelancer profile edit and onboarding pages provide direct S3 file upload buttons with instant preview alongside URL input fallback.
